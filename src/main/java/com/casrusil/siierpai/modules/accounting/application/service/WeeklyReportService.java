package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.mail.EmailService;
import com.casrusil.siierpai.shared.infrastructure.reporting.ExcelExportService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class WeeklyReportService {

    private final SearchInvoicesUseCase searchInvoicesUseCase;
    private final BalanceSheetService balanceSheetService;
    private final EmailService emailService;

    public WeeklyReportService(SearchInvoicesUseCase searchInvoicesUseCase, BalanceSheetService balanceSheetService,
            EmailService emailService) {
        this.searchInvoicesUseCase = searchInvoicesUseCase;
        this.balanceSheetService = balanceSheetService;
        this.emailService = emailService;
    }

    public void generateAndSendWeeklyReport(CompanyId companyId, String userEmail) {
        // 1. Fetch Data
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(7);

        List<Invoice> invoices = searchInvoicesUseCase.getInvoicesByCompany(companyId); // In prod: filter by date in DB
        List<Invoice> weeklyInvoices = invoices.stream()
                .filter(i -> !i.getDate().isBefore(weekStart) && !i.getDate().isAfter(now))
                .toList();

        long salesCount = weeklyInvoices.stream().filter(i -> i.getTransactionType() == TransactionType.SALE).count();
        long purchasesCount = weeklyInvoices.stream().filter(i -> i.getTransactionType() == TransactionType.PURCHASE)
                .count();

        // 2. Generate PDF (Stub)
        byte[] pdfReport = generatePdfReport(salesCount, purchasesCount, weekStart, now);

        // 3. Send Email
        emailService.sendWeeklyReportEmail(userEmail, pdfReport, weekStart + " to " + now);
    }

    private byte[] generatePdfReport(long sales, long purchases, LocalDate start, LocalDate end) {
        // TODO: Use OpenPDF to generate real PDF. For now, returning dummy bytes.
        // In a real implementation this would use Document, PdfWriter, etc.
        String dummyContent = "Weekly Report from " + start + " to " + end + "\nSales: " + sales + "\nPurchases: "
                + purchases;
        return dummyContent.getBytes();
    }
}
