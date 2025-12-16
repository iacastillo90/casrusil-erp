package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.TaxAuditReport;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Auditoría Tributaria (Tax Compliance) - CORRECCIÓN MAESTRA
 * (Identity Crisis Fix & DTO Nested Structure).
 */
@Service
public class TaxAuditService {

    private final InvoiceRepository invoiceRepository;
    private final AccountingEntryRepository accountingEntryRepository;

    public TaxAuditService(InvoiceRepository invoiceRepository, AccountingEntryRepository accountingEntryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.accountingEntryRepository = accountingEntryRepository;
    }

    public TaxAuditReport performAudit(CompanyId companyId, int year, int month, List<Object> ignoredSiiRecords) {
        YearMonth targetMonth = YearMonth.of(year, month);

        // 1. OBTENER "SII TRUTH" (Facturas Importadas)
        List<Invoice> siiInvoices = invoiceRepository.findByCompanyId(companyId).stream()
                .filter(inv -> {
                    YearMonth invMonth = YearMonth.from(inv.getDate());
                    return invMonth.equals(targetMonth);
                })
                .collect(Collectors.toList());

        // 2. OBTENER "ERP TRUTH" (Asientos Contables)
        List<AccountingEntry> accountingEntries = accountingEntryRepository.findByCompanyId(companyId).stream()
                .filter(entry -> {
                    YearMonth entryMonth = YearMonth.from(entry.getEntryDate());
                    return entryMonth.equals(targetMonth);
                })
                .collect(Collectors.toList());

        // 3. INDEXAR Asientos
        Map<String, AccountingEntry> accountingMap = new HashMap<>();
        for (AccountingEntry entry : accountingEntries) {
            if (entry.getDocumentNumber() != null && entry.getDocumentType() != null) {
                String key = buildKey(
                        entry.getTaxPayerId(),
                        parseDocType(entry.getDocumentType()),
                        parseFolio(entry.getDocumentNumber()));
                accountingMap.put(key, entry);
            }
        }

        List<TaxAuditReport.DiscrepancyDetail> discrepancies = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();

        BigDecimal totalSii = BigDecimal.ZERO;
        BigDecimal totalErp = BigDecimal.ZERO;

        // 4. COMPARAR: SII -> ERP
        for (Invoice invoice : siiInvoices) {
            String key = buildKey(invoice.getIssuerRut(), invoice.getType().getCode(), invoice.getFolio());
            processedKeys.add(key);

            // Sumar al acumulado SII (usamos TaxAmount para simular IVA, o 0 si nulo)
            BigDecimal currentIva = invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO;
            totalSii = totalSii.add(currentIva);

            String status = "OK";
            BigDecimal erpAmount = BigDecimal.ZERO;
            // Definir Tipo (Compra/Venta)
            String type = "UNKNOWN";
            if (invoice.getTransactionType() != null) {
                type = invoice.getTransactionType().name(); // SALE / PURCHASE
            } else {
                type = (invoice.getType().getCode() == 33 || invoice.getType().getCode() == 34) ? "VENTA" : "COMPRA";
            }

            if (accountingMap.containsKey(key)) {
                // MATCH
                AccountingEntry entry = accountingMap.get(key);
                erpAmount = calculateEntryAmount(entry);

                // Diff > 5 pesos
                BigDecimal diff = invoice.getTotalAmount().subtract(erpAmount).abs();
                if (diff.compareTo(new BigDecimal("5")) > 0) {
                    status = "DIFERENCIA_MONTO";
                }
                totalErp = totalErp.add(erpAmount);
            } else {
                status = "NO_EN_ERP";
            }

            discrepancies.add(new TaxAuditReport.DiscrepancyDetail(
                    type,
                    invoice.getDate(),
                    invoice.getBusinessName() != null ? invoice.getBusinessName() : "S/N",
                    invoice.getFolio(),
                    invoice.getTotalAmount(), // Monto SII
                    erpAmount, // Monto ERP
                    status));
        }

        // 5. DETECTAR FANTASMAS (ERP -> SII)
        for (AccountingEntry entry : accountingEntries) {
            if (entry.getDocumentNumber() != null && entry.getDocumentType() != null) {
                String key = buildKey(
                        entry.getTaxPayerId(),
                        parseDocType(entry.getDocumentType()),
                        parseFolio(entry.getDocumentNumber()));

                if (!processedKeys.contains(key)) {
                    BigDecimal entryVal = calculateEntryAmount(entry);
                    discrepancies.add(new TaxAuditReport.DiscrepancyDetail(
                            "MANUAL",
                            entry.getEntryDate(),
                            entry.getTaxPayerName(),
                            parseFolio(entry.getDocumentNumber()),
                            BigDecimal.ZERO,
                            entryVal,
                            "NO_EN_SII"));
                    totalErp = totalErp.add(entryVal);
                }
            }
        }

        TaxAuditReport.AuditSummary summary = new TaxAuditReport.AuditSummary(
                totalSii,
                totalErp, // Nota: Aqui sumamos Total ERP vs Tax SII (porque no tenemos desglose facil del
                          // IVA en asiento)
                totalSii.subtract(totalErp),
                siiInvoices.size(),
                (int) discrepancies.stream().filter(d -> !d.status().equals("OK")).count());

        return new TaxAuditReport(summary, discrepancies);
    }

    private BigDecimal calculateEntryAmount(AccountingEntry entry) {
        // Suma de débitos como proxy del monto total del asiento
        return entry.getLines().stream().map(l -> l.debit()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String buildKey(String rut, Integer type, Long folio) {
        return String.format("%s-%d-%d", normalizeRut(rut), type, folio);
    }

    private String normalizeRut(String rut) {
        if (rut == null)
            return "UNKNOWN";
        return rut.replace(".", "").replace("-", "").toUpperCase();
    }

    private Integer parseDocType(String docType) {
        try {
            return Integer.parseInt(docType);
        } catch (Exception e) {
            return 0;
        }
    }

    private Long parseFolio(String folio) {
        try {
            return Long.parseLong(folio);
        } catch (Exception e) {
            return 0L;
        }
    }
}
