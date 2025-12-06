package com.casrusil.SII_ERP_AI.modules.accounting.application.listener;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener que genera asientos contables automáticamente cuando se crea una
 * factura.
 * 
 * <p>
 * Escucha eventos {@link InvoiceCreatedEvent} y crea los asientos contables
 * correspondientes
 * aplicando reglas de clasificación aprendidas del
 * {@link com.casrusil.SII_ERP_AI.modules.accounting.application.service.LearningService}.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Escuchar {@link InvoiceCreatedEvent} de forma asíncrona</li>
 * <li>Generar asientos contables según tipo de DTE</li>
 * <li>Aplicar reglas de clasificación aprendidas</li>
 * <li>Garantizar partida doble (Débito = Crédito)</li>
 * <li>Ejecutar en contexto de empresa correcta (multi-tenancy)</li>
 * </ul>
 * 
 * <h2>Flujo de generación:</h2>
 * <ol>
 * <li>Factura creada → Evento publicado</li>
 * <li>Listener captura evento (asíncrono)</li>
 * <li>Determina cuentas contables según tipo de DTE</li>
 * <li>Aplica reglas aprendidas para clasificación</li>
 * <li>Crea asiento balanceado (Débito = Crédito)</li>
 * <li>Persiste asiento vía {@link AccountingEntryService}</li>
 * </ol>
 * 
 * <h2>Ejemplo de asiento generado:</h2>
 * 
 * <pre>
 * Factura Afecta (33) - Compra:
 * Débito:  510101 (Gastos)        $100,000
 * Débito:  110401 (IVA Crédito)   $19,000
 * Crédito: 210101 (Proveedores)   $119,000
 * </pre>
 * 
 * @see InvoiceCreatedEvent
 * @see AccountingEntryService
 * @see com.casrusil.SII_ERP_AI.modules.accounting.application.service.LearningService
 * @since 1.0
 */
@Component
public class InvoiceAccountingListener {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceAccountingListener.class);
    private final AccountingEntryService accountingEntryService;
    private final com.casrusil.SII_ERP_AI.modules.accounting.application.service.LearningService learningService;

    public InvoiceAccountingListener(AccountingEntryService accountingEntryService,
            com.casrusil.SII_ERP_AI.modules.accounting.application.service.LearningService learningService) {
        this.accountingEntryService = accountingEntryService;
        this.learningService = learningService;
    }

    @Async
    @EventListener
    public void handle(InvoiceCreatedEvent event) {
        Invoice invoice = event.invoice();

        // Run in company context
        CompanyContext.runInCompanyContext(invoice.getCompanyId(), () -> {
            AccountingEntry entry = createEntryFromInvoice(invoice);
            accountingEntryService.recordEntry(entry);
        });
    }

    private AccountingEntry createEntryFromInvoice(Invoice invoice) {
        List<AccountingEntryLine> lines = new ArrayList<>();

        // 🧠 FEEDBACK LOOP: Check if we have learned rules for this invoice
        String description = "Invoice " + invoice.getFolio() + " - " + invoice.getIssuerRut();
        List<com.casrusil.SII_ERP_AI.modules.accounting.domain.model.ClassificationRule> applicableRules = learningService
                .findApplicableRules(invoice.getCompanyId(), description);

        // Determine account codes (learned or default)
        // Determine account codes (learned or default)
        String accountCode;
        if (!applicableRules.isEmpty()) {
            // Use learned rule
            accountCode = applicableRules.get(0).getAccountCode();
            logger.info("🎓 Applying learned rule: {} -> {}", applicableRules.get(0).getPattern(), accountCode);
        } else {
            // Use default logic
            if (invoice
                    .getTransactionType() == com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType.SALE) {
                accountCode = "310101"; // Ventas (default)
            } else {
                accountCode = "510101"; // Gastos Generales (default for purchase)
            }
        }

        if (invoice
                .getTransactionType() == com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType.SALE) {
            // SALE Logic (Issued)
            String accountsReceivableCode = "110501"; // Clientes Nacionales
            String vatPayableCode = "210401"; // IVA Débito Fiscal

            lines.add(AccountingEntryLine.debit(accountsReceivableCode, invoice.getTotalAmount()));

            if (invoice.getNetAmount().compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.credit(accountCode, invoice.getNetAmount()));
            }

            if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.credit(vatPayableCode, invoice.getTaxAmount()));
            }
        } else {
            // PURCHASE Logic (Received)
            String accountsPayableCode = "210101"; // Proveedores
            String vatCreditCode = "110401"; // IVA Crédito Fiscal

            if (invoice.getNetAmount().compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.debit(accountCode, invoice.getNetAmount()));
            }

            if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                lines.add(AccountingEntryLine.debit(vatCreditCode, invoice.getTaxAmount()));
            }

            lines.add(AccountingEntryLine.credit(accountsPayableCode, invoice.getTotalAmount()));
        }

        // Handle exempt invoices where Net might be Total and VAT is 0, or logic
        // differs.
        // For now, this simple logic covers standard VAT invoices.
        // If Total != Net + VAT, we might have rounding issues or exempt amounts.
        // The validator will catch if not balanced.

        return new AccountingEntry(
                invoice.getCompanyId(),
                description,
                invoice.getId().toString(),
                "INVOICE",
                lines,
                com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType.NORMAL);
    }
}
