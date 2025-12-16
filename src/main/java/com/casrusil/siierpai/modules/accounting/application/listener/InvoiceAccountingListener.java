package com.casrusil.siierpai.modules.accounting.application.listener;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.ClassificationRule;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class InvoiceAccountingListener {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceAccountingListener.class);
    private final AccountingEntryService accountingEntryService;
    private final com.casrusil.siierpai.modules.accounting.application.service.LearningService learningService;

    // Standard Chilean Account Plan Constants
    private static final String ACCOUNT_SALES_REVENUE = "410101"; // Ventas Afectas (Ingresos)
    private static final String ACCOUNT_VAT_DEBIT = "210401"; // IVA Débito (Pasivo)
    private static final String ACCOUNT_EXPENSES = "510101"; // Gastos Generales (Pérdida)
    private static final String ACCOUNT_VAT_CREDIT = "110801"; // IVA Crédito Fiscal (Activo)
    private static final String ACCOUNT_RECEIVABLE = "110501"; // Clientes (Activo)
    private static final String ACCOUNT_PAYABLE = "210101"; // Proveedores (Pasivo)

    public InvoiceAccountingListener(AccountingEntryService accountingEntryService,
            com.casrusil.siierpai.modules.accounting.application.service.LearningService learningService) {
        this.accountingEntryService = accountingEntryService;
        this.learningService = learningService;
    }

    @Async
    @EventListener
    public void handle(InvoiceCreatedEvent event) {
        Invoice invoice = event.invoice();

        CompanyContext.runInCompanyContext(invoice.getCompanyId(), () -> {
            try {
                AccountingEntry entry = createEntryFromInvoice(invoice);
                accountingEntryService.recordEntry(entry);
                logger.info("✅ Asiento contable creado para factura folio {}", invoice.getFolio());
            } catch (Exception e) {
                logger.error("❌ Error creando asiento para factura {}: {}", invoice.getFolio(), e.getMessage());
            }
        });
    }

    private AccountingEntry createEntryFromInvoice(Invoice invoice) {
        List<AccountingEntryLine> lines = new ArrayList<>();

        String description = String.format("DTE %s Folio %d | %s | %s",
                invoice.getType().getCode(),
                invoice.getFolio(),
                invoice.getIssuerRut(),
                invoice.getBusinessName() != null ? invoice.getBusinessName() : "Unknown");

        boolean isCreditNote = invoice.getType().getCode() == 61;

        var applicableRules = learningService.findApplicableRules(invoice.getCompanyId(), description);

        // 1. Determine Expense/Revenue Account
        String accountCode;
        if (!applicableRules.isEmpty()) {
            accountCode = applicableRules.get(0).getAccountCode();
            logger.info("🎓 Applying learned rule: {} -> {}", applicableRules.get(0).getPattern(), accountCode);
        } else {
            if (invoice
                    .getTransactionType() == com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.SALE) {
                accountCode = ACCOUNT_SALES_REVENUE; // Fixed: Now 410101 (Revenue)
            } else {
                accountCode = ACCOUNT_EXPENSES; // Fixed: Now 510101 (Expense)
            }
        }

        // 2. Calculate Exempt Amount
        BigDecimal exemptAmount = invoice.getTotalAmount()
                .subtract(invoice.getNetAmount())
                .subtract(invoice.getTaxAmount());

        String taxPayerId;
        String taxPayerName = invoice.getBusinessName();

        if (invoice.getTransactionType() == com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.SALE) {
            // === SALE ===
            taxPayerId = invoice.getReceiverRut();
            String accountsReceivableName = "Clientes";
            String vatPayableName = "IVA Débito Fiscal";
            String revenueName = "Ventas";

            // Debit: Client owes Total
            addDebit(lines, isCreditNote, ACCOUNT_RECEIVABLE, accountsReceivableName, invoice.getTotalAmount());

            // Credit: Net Revenue
            if (invoice.getNetAmount().compareTo(BigDecimal.ZERO) > 0) {
                addCredit(lines, isCreditNote, accountCode, revenueName, invoice.getNetAmount());
            }
            // Credit: Exempt Revenue
            if (exemptAmount.compareTo(BigDecimal.ZERO) > 0) {
                addCredit(lines, isCreditNote, accountCode, revenueName, exemptAmount);
            }
            // Credit: VAT Payable
            if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                addCredit(lines, isCreditNote, ACCOUNT_VAT_DEBIT, vatPayableName, invoice.getTaxAmount());
            }

        } else {
            // === PURCHASE ===
            taxPayerId = invoice.getIssuerRut();
            String accountsPayableName = "Proveedores";
            String vatCreditName = "IVA Crédito Fiscal";
            String vatCommon = "110502";
            String vatCommonName = "IVA Uso Común";
            String fixedAssetAccount = "150101";
            String fixedAssetAccountName = "Maquinaria y Equipos";
            String expenseName = "Gasto";

            // 1. Fixed Asset
            BigDecimal fixedAssetAmount = invoice.getFixedAssetAmount();
            if (fixedAssetAmount.compareTo(BigDecimal.ZERO) > 0) {
                addDebit(lines, isCreditNote, fixedAssetAccount, fixedAssetAccountName, fixedAssetAmount);
            }

            // 2. Expense (Net - Fixed Asset)
            BigDecimal expenseAmount = invoice.getNetAmount().subtract(fixedAssetAmount);
            if (expenseAmount.compareTo(BigDecimal.ZERO) > 0) {
                addDebit(lines, isCreditNote, accountCode, expenseName, expenseAmount);
            }

            // Debit: Exempt Expense
            if (exemptAmount.compareTo(BigDecimal.ZERO) > 0) {
                addDebit(lines, isCreditNote, accountCode, expenseName, exemptAmount);
            }

            // 3. VAT
            BigDecimal commonVat = invoice.getCommonUseVatAmount();
            BigDecimal standardVat = invoice.getTaxAmount().subtract(commonVat);

            // VAT Common Use
            if (commonVat.compareTo(BigDecimal.ZERO) > 0) {
                addDebit(lines, isCreditNote, vatCommon, vatCommonName, commonVat);
            }
            // VAT Credit Fiscal
            if (standardVat.compareTo(BigDecimal.ZERO) > 0) {
                // Fixed: Uses ACCOUNT_VAT_CREDIT (110801)
                addDebit(lines, isCreditNote, ACCOUNT_VAT_CREDIT, vatCreditName, standardVat);
            }

            // Credit: I owe the Supplier
            addCredit(lines, isCreditNote, ACCOUNT_PAYABLE, accountsPayableName, invoice.getTotalAmount());
        }

        return new AccountingEntry(
                invoice.getCompanyId(),
                invoice.getDate(),
                description + (isCreditNote ? " (NC)" : ""),
                invoice.getId().toString(),
                "INVOICE",
                taxPayerId,
                taxPayerName,
                String.valueOf(invoice.getType().getCode()),
                String.valueOf(invoice.getFolio()),
                "POSTED",
                lines,
                com.casrusil.siierpai.modules.accounting.domain.model.EntryType.NORMAL);
    }

    /**
     * Adds a Debit line normally. If it's a Credit Note, adds a Credit line
     * instead.
     */
    private void addDebit(List<AccountingEntryLine> lines, boolean isCreditNote, String code, String name,
            BigDecimal amount) {
        if (isCreditNote) {
            lines.add(AccountingEntryLine.credit(code, name, amount));
        } else {
            lines.add(AccountingEntryLine.debit(code, name, amount));
        }
    }

    /**
     * Adds a Credit line normally. If it's a Credit Note, adds a Debit line
     * instead.
     */
    private void addCredit(List<AccountingEntryLine> lines, boolean isCreditNote, String code, String name,
            BigDecimal amount) {
        if (isCreditNote) {
            lines.add(AccountingEntryLine.debit(code, name, amount));
        } else {
            lines.add(AccountingEntryLine.credit(code, name, amount));
        }
    }
}
