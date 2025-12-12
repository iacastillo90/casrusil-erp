package com.casrusil.siierpai.modules.accounting.application.listener;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
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

        // AJUSTE: Concatenar info en descripción con los nuevos datos
        String description = String.format("DTE %s Folio %d | %s | %s",
                invoice.getType().getCode(),
                invoice.getFolio(),
                invoice.getIssuerRut(),
                invoice.getBusinessName() != null ? invoice.getBusinessName() : "Unknown");

        var applicableRules = learningService.findApplicableRules(invoice.getCompanyId(), description);

        // 1. Determinar Cuenta de Gasto/Ingreso
        String accountCode;
        if (!applicableRules.isEmpty()) {
            accountCode = applicableRules.get(0).getAccountCode();
            logger.info("🎓 Applying learned rule: {} -> {}", applicableRules.get(0).getPattern(), accountCode);
        } else {
            if (invoice
                    .getTransactionType() == com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.SALE) {
                accountCode = "310101"; // Ventas (default)
            } else {
                accountCode = "510101"; // Costo de Ventas / Gastos (default)
            }
        }

        // 2. Calcular Monto Exento (Total - Neto - IVA)
        BigDecimal exemptAmount = invoice.getTotalAmount()
                .subtract(invoice.getNetAmount())
                .subtract(invoice.getTaxAmount());

        String taxPayerId;
        String taxPayerName = invoice.getBusinessName();

        if (invoice.getTransactionType() == com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.SALE) {
            // === VENTA ===
            taxPayerId = invoice.getReceiverRut();
            String accountsReceivable = "110501";
            String accountsReceivableName = "Clientes";
            String vatPayable = "210401";
            String vatPayableName = "IVA Débito Fiscal";
            String revenueName = "Ventas";

            // Debe: Cliente me debe el Total
            lines.add(AccountingEntryLine.debit(accountsReceivable, accountsReceivableName, invoice.getTotalAmount()));

            // Haber: Ingreso Neto
            if (invoice.getNetAmount().compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.credit(accountCode, revenueName, invoice.getNetAmount()));
            }
            // Haber: Ingreso Exento (si existe)
            if (exemptAmount.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.credit(accountCode, revenueName, exemptAmount));
            }
            // Haber: IVA por Pagar
            if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.credit(vatPayable, vatPayableName, invoice.getTaxAmount()));
            }

        } else {
            // === COMPRA ===
            taxPayerId = invoice.getIssuerRut();
            String accountsPayable = "210101";
            String accountsPayableName = "Proveedores";
            String vatCredit = "110801"; // IVA Crédito Fiscal del Mes
            String vatCreditName = "IVA Crédito Fiscal";
            String vatCommon = "110502";
            String vatCommonName = "IVA Uso Común";
            String fixedAssetAccount = "150101";
            String fixedAssetAccountName = "Maquinaria y Equipos";
            String expenseName = "Gasto"; // O "Costo de Ventas" si aplica

            // 1. Activo Fijo
            BigDecimal fixedAssetAmount = invoice.getFixedAssetAmount();
            if (fixedAssetAmount.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.debit(fixedAssetAccount, fixedAssetAccountName, fixedAssetAmount));
            }

            // 2. Gasto (Neto - Activo Fijo)
            BigDecimal expenseAmount = invoice.getNetAmount().subtract(fixedAssetAmount);
            if (expenseAmount.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.debit(accountCode, expenseName, expenseAmount));
            }

            // Debe: Gasto Exento
            if (exemptAmount.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.debit(accountCode, expenseName, exemptAmount));
            }

            // 3. IVA
            BigDecimal commonVat = invoice.getCommonUseVatAmount();
            BigDecimal standardVat = invoice.getTaxAmount().subtract(commonVat);

            // IVA Uso Común
            if (commonVat.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.debit(vatCommon, vatCommonName, commonVat));
            }
            // IVA Crédito Fiscal
            if (standardVat.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.debit(vatCredit, vatCreditName, standardVat));
            }

            // Haber: Le debo al Proveedor el Total
            lines.add(AccountingEntryLine.credit(accountsPayable, accountsPayableName, invoice.getTotalAmount()));
        }

        return new AccountingEntry(
                invoice.getCompanyId(),
                invoice.getDate(), // <--- CAMBIO CRÍTICO: Fecha real del documento
                description,
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
}
