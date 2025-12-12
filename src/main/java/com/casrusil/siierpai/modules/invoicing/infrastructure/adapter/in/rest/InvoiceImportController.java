package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.casrusil.siierpai.modules.fees.application.service.FeeReceiptService;

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
    private final CompanyRepository companyRepository;
    private final FeeReceiptService feeReceiptService;

    public InvoiceImportController(CreateInvoiceUseCase createInvoiceUseCase, CompanyRepository companyRepository,
            FeeReceiptService feeReceiptService) {
        this.createInvoiceUseCase = createInvoiceUseCase;
        this.companyRepository = companyRepository;
        this.feeReceiptService = feeReceiptService;
    }

    @PostMapping(value = { "", "/sii-csv" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importSiiCsv(@RequestParam("file") MultipartFile file,
            @RequestParam("companyId") UUID companyId,
            @RequestParam("bookType") String bookType) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ImportResult(0, 0, "File is empty"));
        }

        // 0. Delegar a Módulo de Honorarios
        if ("FEES".equalsIgnoreCase(bookType) || "HONORARIOS".equalsIgnoreCase(bookType)) {
            try {
                var result = feeReceiptService.importFromCsv(file.getInputStream(), new CompanyId(companyId));
                return ResponseEntity
                        .ok(new ImportResult(result.successCount(), result.errorCount(), result.message()));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new ImportResult(0, 0, "Fee Import Error: " + e.getMessage()));
            }
        }

        // 1. Determinar Tipo de Transacción
        boolean isSale = "VENTA".equalsIgnoreCase(bookType) || "SALE".equalsIgnoreCase(bookType);
        com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType transactionType = isSale
                ? com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.SALE
                : com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.PURCHASE;

        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        // Fetch company mainly to get own RUT
        Company company = companyRepository.findById(new CompanyId(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        String myCompanyRut = company.getRut();

        // 2. Configurar formato para SII (Punto y coma + Cabeceras)
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        // 3. Formateador de Fecha Chile (d/M/yyyy)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1));
                CSVParser csvParser = new CSVParser(reader, format)) {

            for (CSVRecord record : csvParser) {
                try {
                    // Variables comunes
                    // Nombres de columna típicos del SII: "Tipo Doc", "Folio", "Fecha Docto"
                    Integer typeCode = Integer.parseInt(record.get("Tipo Doc"));
                    Long folio = Long.parseLong(record.get("Folio"));

                    // Parseo de Fecha corregido
                    String fechaStr = record.get("Fecha Docto");
                    // A veces trae hora (ej: "03/11/2025 10:00:00"), aseguramos limpiar
                    if (fechaStr.contains(" ")) {
                        fechaStr = fechaStr.split(" ")[0];
                    }
                    LocalDate date = LocalDate.parse(fechaStr, formatter);

                    // Montos (Manejo de nulos o vacíos usando parseMonto)
                    BigDecimal netAmount = parseMonto(record.get("Monto Neto"));
                    BigDecimal totalAmount = parseMonto(record.get("Monto Total")); // "Monto Total" o "Monto total"
                                                                                    // handled by IgnoreHeaderCase

                    // Lógica diferenciada para COMPRA vs VENTA
                    String issuerRut;
                    String receiverRut;
                    BigDecimal taxAmount;

                    if (!isSale) { // PURCHASE
                        // EN COMPRA: El emisor es el proveedor, el receptor somos nosotros (la empresa
                        // del sistema)
                        issuerRut = record.get("RUT Proveedor");
                        receiverRut = myCompanyRut;

                        // En compras, a veces el IVA es 'Monto IVA Recuperable'
                        String ivaStr = record.isMapped("Monto IVA Recuperable") ? record.get("Monto IVA Recuperable")
                                : "0";
                        // Fallback si no está presente o vacío
                        if (ivaStr == null || ivaStr.isEmpty()) {
                            ivaStr = record.isMapped("Monto IVA") ? record.get("Monto IVA") : "0";
                        }

                        taxAmount = parseMonto(ivaStr);

                    } else { // SALE
                        // EN VENTA: El emisor somos nosotros, el receptor es el cliente
                        issuerRut = myCompanyRut;
                        receiverRut = record.get("Rut cliente");

                        String ivaStr = record.get("Monto IVA");
                        taxAmount = parseMonto(ivaStr);
                    }

                    // Create Invoice with minimal required fields for import
                    Invoice invoice = Invoice.create(
                            new CompanyId(companyId),
                            InvoiceType.fromCode(typeCode),
                            folio,
                            issuerRut,
                            receiverRut,
                            date,
                            netAmount,
                            taxAmount,
                            totalAmount,
                            Invoice.ORIGIN_MANUAL,
                            transactionType,
                            Collections.emptyList());

                    // Guardar
                    com.casrusil.siierpai.shared.infrastructure.context.CompanyContext
                            .runInCompanyContext(new CompanyId(companyId), () -> {
                                createInvoiceUseCase.createInvoice(invoice);
                            });
                    successCount++;

                } catch (Exception e) {
                    logger.error("Error parsing row " + record.getRecordNumber(), e);
                    errorCount++;
                    errors.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ImportResult(0, 0, "Critical error: " + e.getMessage()));
        }

        return ResponseEntity.ok(new ImportResult(successCount, errorCount, "Import completed"));
    }

    // Helper para parsear montos vacíos o ceros
    private BigDecimal parseMonto(String value) {
        if (value == null || value.trim().isEmpty())
            return BigDecimal.ZERO;
        return new BigDecimal(value.trim().replace(".", "").replace(",", ".")); // Limpieza básica CLP
    }

    public record ImportResult(int successCount, int errorCount, String message) {
    }
}
