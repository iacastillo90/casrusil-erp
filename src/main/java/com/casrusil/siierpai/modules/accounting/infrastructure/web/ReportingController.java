package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29SenderService;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.casrusil.siierpai.shared.infrastructure.reporting.ExcelExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para reportes financieros y fiscales.
 * 
 * <p>
 * Centraliza la generación de reportes (Excel, Balance, F29) para su consumo
 * por el frontend o descarga directa.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code GET /api/v1/reports/sales/excel}: Descargar reporte de ventas en
 * Excel.</li>
 * <li>{@code GET /api/v1/reports/balance-sheet}: Obtener Balance General.</li>
 * <li>{@code POST /api/v1/reports/f29/submit}: Enviar declaración F29 al
 * SII.</li>
 * </ul>
 * 
 * @see ExcelExportService
 * @see BalanceSheetService
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

    private final SearchInvoicesUseCase searchInvoicesUseCase;
    private final ExcelExportService excelExportService;
    private final BalanceSheetService balanceSheetService;
    private final F29CalculatorService f29CalculatorService;
    private final F29SenderService f29SenderService;

    public ReportingController(SearchInvoicesUseCase searchInvoicesUseCase,
            ExcelExportService excelExportService,
            BalanceSheetService balanceSheetService,
            F29CalculatorService f29CalculatorService,
            F29SenderService f29SenderService) {
        this.searchInvoicesUseCase = searchInvoicesUseCase;
        this.excelExportService = excelExportService;
        this.balanceSheetService = balanceSheetService;
        this.f29CalculatorService = f29CalculatorService;
        this.f29SenderService = f29SenderService;
    }

    @GetMapping("/sales/excel")
    public ResponseEntity<byte[]> downloadSalesExcel(@RequestParam(name = "period", required = false) String periodStr)
            throws IOException {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth period = (periodStr != null) ? YearMonth.parse(periodStr) : YearMonth.now();

        List<Invoice> invoices = searchInvoicesUseCase.getInvoicesByCompany(companyId);
        // Filter by period
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        List<Invoice> filtered = invoices.stream()
                .filter(inv -> !inv.getDate().isBefore(start) && !inv.getDate().isAfter(end))
                .toList();

        List<String> headers = List.of("Folio", "RUT Emisor", "Fecha", "Monto Neto", "IVA", "Total");
        List<List<Object>> data = new ArrayList<>();
        for (Invoice inv : filtered) {
            data.add(List.of(
                    inv.getFolio(),
                    inv.getIssuerRut(),
                    inv.getDate(),
                    inv.getNetAmount(),
                    inv.getTaxAmount(),
                    inv.getTotalAmount()));
        }

        byte[] excelBytes = excelExportService.generateExcel("Ventas " + period, headers, data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_report_" + period + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetReport> getBalanceSheet(
            @RequestParam(name = "date", required = false) String dateStr) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        LocalDate date = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();

        BalanceSheetReport report = balanceSheetService.generateBalanceSheet(companyId, date);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/f29/submit")
    public ResponseEntity<Map<String, String>> submitF29(@RequestParam("period") String periodStr) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth period = YearMonth.parse(periodStr);

        F29Report report = f29CalculatorService.calculateF29(companyId, period);
        String transactionId = f29SenderService.sendDeclaration(companyId, report);

        return ResponseEntity.ok(Map.of(
                "status", "SUBMITTED",
                "transactionId", transactionId,
                "period", period.toString(),
                "amountToPay", report.vatPayable().toString()));
    }
}
