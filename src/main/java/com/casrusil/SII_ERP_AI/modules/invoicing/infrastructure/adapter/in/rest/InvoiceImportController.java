package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
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

    public InvoiceImportController(CreateInvoiceUseCase createInvoiceUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
    }

    @PostMapping(value = { "", "/sii-csv" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importSiiCsv(@RequestParam("file") MultipartFile file,
            @RequestParam("companyId") UUID companyId,
            @RequestParam("bookType") String bookType) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ImportResult(0, 0, "File is empty"));
        }

        com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType transactionType;
        if ("COMPRA".equalsIgnoreCase(bookType) || "PURCHASE".equalsIgnoreCase(bookType)) {
            transactionType = com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType.PURCHASE;
        } else if ("VENTA".equalsIgnoreCase(bookType) || "SALE".equalsIgnoreCase(bookType)) {
            transactionType = com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType.SALE;
        } else {
            return ResponseEntity.badRequest()
                    .body(new ImportResult(0, 0, "Invalid bookType. Must be PURCHASE/COMPRA or SALE/VENTA"));
        }

        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    // Parse CSV record to Invoice
                    // Assuming columns: Tipo Doc, Folio, RUT Emisor, RUT Receptor, Fecha Emision,
                    // Monto Neto, Monto IVA, Monto Total
                    // Adjust column names based on actual SII CSV format if known, otherwise using
                    // generic names

                    // Mapping logic (simplified for demo)
                    Integer typeCode = Integer.parseInt(record.get("Tipo Doc"));
                    Long folio = Long.parseLong(record.get("Folio"));
                    String issuerRut = record.get("RUT Emisor");
                    String receiverRut = record.get("RUT Receptor");
                    LocalDate date = LocalDate.parse(record.get("Fecha Emision"),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Adjust format as needed
                    BigDecimal netAmount = new BigDecimal(record.get("Monto Neto"));
                    BigDecimal taxAmount = new BigDecimal(record.get("Monto IVA"));
                    BigDecimal totalAmount = new BigDecimal(record.get("Monto Total"));

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
                            Collections.emptyList() // No items in simple CSV import
                    );

                    // Run in company context to satisfy InvoiceManagementService checks
                    com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext
                            .runInCompanyContext(new CompanyId(companyId), () -> {
                                createInvoiceUseCase.createInvoice(invoice);
                            });
                    successCount++;

                } catch (Exception e) {
                    logger.error("Error parsing/saving record: " + record, e);
                    errorCount++;
                    errors.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error processing CSV file", e);
            return ResponseEntity.internalServerError()
                    .body(new ImportResult(0, 0, "Error processing file: " + e.getMessage()));
        }

        return ResponseEntity.ok(new ImportResult(successCount, errorCount, "Import completed"));
    }

    public record ImportResult(int successCount, int errorCount, String message) {
    }
}
