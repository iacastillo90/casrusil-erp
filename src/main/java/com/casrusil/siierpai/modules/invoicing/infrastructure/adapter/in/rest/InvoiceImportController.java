package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices/import")
public class InvoiceImportController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceImportController.class);
    private final CreateInvoiceUseCase createInvoiceUseCase;

    // Formato fecha SII: dd/MM/yyyy (ej: 30/09/2025)
    private static final DateTimeFormatter SII_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public InvoiceImportController(CreateInvoiceUseCase createInvoiceUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
    }

    @PostMapping(value = { "", "/sii-csv" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importSiiCsv(@RequestParam("file") MultipartFile file,
            @RequestParam("companyId") UUID companyId,
            @RequestParam("bookType") String bookType) {

        if (file.isEmpty())
            return ResponseEntity.badRequest().body(new ImportResult(0, 0, "Archivo vacío"));

        TransactionType transactionType;
        if ("COMPRA".equalsIgnoreCase(bookType) || "PURCHASE".equalsIgnoreCase(bookType)) {
            transactionType = TransactionType.PURCHASE;
        } else if ("VENTA".equalsIgnoreCase(bookType) || "SALE".equalsIgnoreCase(bookType)) {
            transactionType = TransactionType.SALE;
        } else {
            return ResponseEntity.badRequest()
                    .body(new ImportResult(0, 0, "Tipo de libro inválido. Use COMPRA o VENTA"));
        }

        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        // Configuración CSV para formato SII (delimitador punto y coma)
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1)); // SII suele usar ISO-8859-1
                                                                                            // (ANSI)
                CSVParser csvParser = new CSVParser(reader, format)) {

            for (CSVRecord record : csvParser) {
                try {
                    // Mapeo dinámico según tipo de libro (Compra vs Venta tienen nombres de columna
                    // distintos)
                    Integer typeCode = Integer.parseInt(record.get("Tipo Doc"));
                    Long folio = Long.parseLong(record.get("Folio"));

                    // SII usa "Fecha Docto" en ambos, pero a veces varía
                    String dateStr = record.isMapped("Fecha Docto") ? record.get("Fecha Docto")
                            : record.get("Fecha Emision");
                    LocalDate date = LocalDate.parse(dateStr, SII_DATE_FORMATTER);

                    String issuerRut;
                    String receiverRut;

                    if (transactionType == TransactionType.PURCHASE) {
                        issuerRut = record.get("RUT Proveedor");
                        receiverRut = "EMPRESA_PROPIA"; // O obtener RUT empresa actual
                    } else {
                        issuerRut = "EMPRESA_PROPIA";
                        receiverRut = record.get("Rut cliente");
                    }

                    // Montos (Manejo de nulos y nombres variados)
                    BigDecimal netAmount = parseAmount(record, "Monto Neto");
                    BigDecimal taxAmount = parseAmount(record, "Monto IVA", "Monto IVA Recuperable");
                    BigDecimal totalAmount = parseAmount(record, "Monto Total", "Monto total"); // Ojo
                                                                                                // mayúscula/minúscula

                    // Extraer Razón Social
                    String businessName = null;
                    if (record.isMapped("Razon Social")) {
                        businessName = record.get("Razon Social");
                    } else if (record.isMapped("Razón Social")) {
                        businessName = record.get("Razón Social");
                    }

                    Invoice invoice = Invoice.create(
                            new CompanyId(companyId),
                            InvoiceType.fromCode(typeCode),
                            folio,
                            issuerRut,
                            receiverRut,
                            businessName, // ✅ Nuevo campo
                            date,
                            netAmount,
                            taxAmount,
                            totalAmount,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            Invoice.ORIGIN_MANUAL,
                            transactionType,
                            Collections.emptyList());

                    CompanyContext.runInCompanyContext(new CompanyId(companyId), () -> {
                        createInvoiceUseCase.createInvoice(invoice);
                    });
                    successCount++;

                } catch (Exception e) {
                    // Ignorar líneas de resumen o vacías que no sean facturas válidas
                    logger.warn("Saltando registro fila {}: {}", record.getRecordNumber(), e.getMessage());
                    errorCount++;
                }
            }

        } catch (Exception e) {
            logger.error("Error procesando CSV SII", e);
            return ResponseEntity.internalServerError()
                    .body(new ImportResult(0, 0, "Error crítico: " + e.getMessage()));
        }

        return ResponseEntity.ok(new ImportResult(successCount, errorCount, "Importación finalizada"));
    }

    private BigDecimal parseAmount(CSVRecord record, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            // Check if mapped AND has value (case insensitive check logic is in parser
            // options)
            if (record.isMapped(header)) {
                String val = record.get(header);
                if (val != null && !val.trim().isEmpty()) {
                    try {
                        // Remove thousands separators (.) and replace decimal separator (,) with (.)
                        String clean = val.replace(".", "").replace(",", ".");
                        return new BigDecimal(clean);
                    } catch (NumberFormatException e) {
                        // Log warning if needed, or return ZERO
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public record ImportResult(int successCount, int errorCount, String message) {
    }
}
