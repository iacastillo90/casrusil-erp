package com.casrusil.siierpai.modules.banking.application.service;

import com.casrusil.siierpai.modules.banking.application.dto.MatchSuggestionDTO;
import com.casrusil.siierpai.modules.banking.application.dto.ReconciliationDashboardDTO;
import com.casrusil.siierpai.modules.banking.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.banking.domain.port.out.BankTransactionRepository;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.PaymentStatus;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;

@Service
public class BankReconciliationWorkbenchService {

    private final BankTransactionRepository bankTransactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository accountingEntryRepository;
    private final BankStatementParser bankStatementParser;

    public BankReconciliationWorkbenchService(BankTransactionRepository bankTransactionRepository,
            InvoiceRepository invoiceRepository,
            com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository accountingEntryRepository,
            BankStatementParser bankStatementParser) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.accountingEntryRepository = accountingEntryRepository;
        this.bankStatementParser = bankStatementParser;
    }

    public ReconciliationDashboardDTO getDashboard(CompanyId companyId) {
        // 1. Fetch Unmatched Bank Lines
        List<BankTransaction> bankLines = bankTransactionRepository.findUnreconciledByCompanyId(companyId);

        // 2. Fetch Unmatched ERP Lines (Pending Invoices)
        List<Invoice> erpLines = invoiceRepository.findByCompanyId(companyId).stream()
                .filter(inv -> inv.getStatus() != PaymentStatus.PAID)
                .toList();

        // 3. Run AI Matching
        List<MatchSuggestionDTO> suggestions = runAiMatching(bankLines, erpLines);

        return new ReconciliationDashboardDTO(bankLines, erpLines, suggestions);
    }

    private List<MatchSuggestionDTO> runAiMatching(List<BankTransaction> bankLines, List<Invoice> erpLines) {
        List<MatchSuggestionDTO> suggestions = new ArrayList<>();
        BigDecimal tolerance = new BigDecimal("1.0");

        for (BankTransaction bank : bankLines) {
            for (Invoice erp : erpLines) {
                // Heuristic 1: Amount Matching (accounting for signs)
                // Bank Inflow (+) <-> Sale Invoice (Total Amount)
                // Bank Outflow (-) <-> Purchase Invoice (Total Amount)

                boolean possibleMatch = false;
                if (bank.getAmount().signum() > 0 && erp.getTransactionType() == TransactionType.SALE) {
                    if (isAmountClose(bank.getAmount(), erp.getTotalAmount(), tolerance)) {
                        possibleMatch = true;
                    }
                } else if (bank.getAmount().signum() < 0 && erp.getTransactionType() == TransactionType.PURCHASE) {
                    if (isAmountClose(bank.getAmount().abs(), erp.getTotalAmount(), tolerance)) {
                        possibleMatch = true;
                    }
                }

                if (possibleMatch) {
                    // Heuristic 2: Date Proximity (within 5 days)
                    long daysDiff = Math.abs(ChronoUnit.DAYS.between(bank.getDate(), erp.getDate()));
                    if (daysDiff <= 5) {
                        suggestions.add(new MatchSuggestionDTO(
                                bank.getId(),
                                erp.getId(),
                                bank.getAmount().abs().subtract(erp.getTotalAmount()).abs(),
                                daysDiff,
                                "HIGH"));
                    }
                }
            }
        }
        return suggestions;
    }

    private boolean isAmountClose(BigDecimal a, BigDecimal b, BigDecimal tolerance) {
        return a.subtract(b).abs().compareTo(tolerance) <= 0;
    }

    @org.springframework.transaction.annotation.Transactional
    public void processMatch(CompanyId companyId, java.util.UUID bankId, java.util.UUID erpId) {
        BankTransaction bank = bankTransactionRepository.findById(bankId);
        Invoice invoice = invoiceRepository.findById(erpId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (bank.isReconciled()) {
            throw new IllegalStateException("Bank transaction already reconciled");
        }
        if (invoice.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Invoice already paid");
        }

        // 1. Mark Invoice as PAID
        invoice.markAsPaid();
        invoiceRepository.save(invoice);

        // 2. Create Accounting Entry (Payment/Collection)
        String taxPayerRut = invoice.getTransactionType() == TransactionType.SALE ? invoice.getReceiverRut()
                : invoice.getIssuerRut();

        List<com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine> lines = new ArrayList<>();
        BigDecimal amount = bank.getAmount().abs();

        if (invoice.getTransactionType() == TransactionType.SALE) {
            // COLLECTION: Debit Bank (110201), Credit Client (110401)
            lines.add(com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine.debit("110201",
                    "Banco Santander", amount));
            lines.add(com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine.credit("110401",
                    "Clientes Nacionales", amount));
        } else {
            // PAYMENT: Debit Supplier (210201), Credit Bank (110201)
            lines.add(com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine.debit("210201",
                    "Proveedores Nacionales", amount));
            lines.add(com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine.credit("110201",
                    "Banco Santander", amount));
        }

        com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry entry = new com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry(
                companyId,
                "Reconciliación: " + bank.getDescription(),
                invoice.getId().toString(),
                "INVOICE",
                taxPayerRut,
                invoice.getBusinessName(),
                String.valueOf(invoice.getType().getCode()),
                String.valueOf(invoice.getFolio()),
                "POSTED",
                lines,
                com.casrusil.siierpai.modules.accounting.domain.model.EntryType.NORMAL);

        accountingEntryRepository.save(entry);

        // 3. Link logic
        bank.markAsReconciled(entry.getId());
        bankTransactionRepository.save(bank);
    }

    public void importBankStatement(java.io.InputStream inputStream, String filename, CompanyId companyId) {
        try {
            List<BankTransaction> transactions;
            String lowerFilename = filename.toLowerCase();

            if (lowerFilename.endsWith(".csv")) {
                transactions = bankStatementParser.parseCsv(inputStream, companyId);
            } else if (lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls")
                    || lowerFilename.endsWith(".xlsb")) {
                transactions = bankStatementParser.parseExcel(inputStream, companyId);
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + filename);
            }

            for (BankTransaction tx : transactions) {
                bankTransactionRepository.save(tx);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to process bank statement file", e);
        }
    }
}
