package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO;
import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO.DiscrepancyItem;
import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO.TaxSummary;
import com.casrusil.siierpai.modules.integration_sii.application.service.SiiRcvService;
import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SiiAuditorService {

    private final InvoiceRepository invoiceRepository;
    private final SiiRcvService siiRcvService;
    private final SiiTokenRepository siiTokenRepository;
    private final CompanyRepository companyRepository;

    public SiiAuditorService(InvoiceRepository invoiceRepository, SiiRcvService siiRcvService,
            SiiTokenRepository siiTokenRepository, CompanyRepository companyRepository) {
        this.invoiceRepository = invoiceRepository;
        this.siiRcvService = siiRcvService;
        this.siiTokenRepository = siiTokenRepository;
        this.companyRepository = companyRepository;
    }

    public SiiAuditReportDTO compareWithSii(CompanyId companyId, int month, int year) {
        // 0. Prepare Data
        String period = String.format("%d%02d", year, month);
        YearMonth targetPeriod = YearMonth.of(year, month);
        String companyRut = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"))
                .getRut(); // Assuming Company has getRut()

        SiiToken token = siiTokenRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalStateException("SII Token not found. Authenticate first."));

        // 1. Fetch Lists
        // Local ERP
        List<Invoice> localInvoices = invoiceRepository.findByCompanyId(companyId).stream()
                .filter(inv -> {
                    // Filter by date (YYYY-MM)
                    YearMonth invPeriod = YearMonth.from(inv.getDate());
                    return invPeriod.equals(targetPeriod);
                })
                .toList();

        // Remote SII
        List<RcvData> purchaseRcv = siiRcvService.downloadPurchaseRegister(token, companyId, companyRut, period);
        List<RcvData> salesRcv = siiRcvService.downloadSalesRegister(token, companyId, companyRut, period);
        List<RcvData> fullSiiList = Stream.concat(purchaseRcv.stream(), salesRcv.stream()).toList();

        // 2. Generate Maps for O(1) Lookup
        // Key: Type-Folio-RutCounterpart (Unique Key for DTE)
        Map<String, Invoice> localMap = localInvoices.stream()
                .collect(Collectors.toMap(this::generateKey, Function.identity(), (a, b) -> a)); // Handle duplicates if
                                                                                                 // any

        Map<String, RcvData> siiMap = fullSiiList.stream()
                .collect(Collectors.toMap(this::generateKey, Function.identity(), (a, b) -> a));

        // 3. Find Discrepancies
        List<DiscrepancyItem> discrepancies = new ArrayList<>();

        // Check SII vs Local (Missing locally?)
        for (RcvData remote : fullSiiList) {
            String key = generateKey(remote);
            if (!localMap.containsKey(key)) {
                discrepancies.add(new DiscrepancyItem(
                        String.valueOf(remote.tipoDte()),
                        remote.folio(),
                        remote.rutEmisor(),
                        remote.fechaEmision(),
                        remote.montoTotal(),
                        BigDecimal.ZERO,
                        "MISSING_IN_ERP",
                        "WARNING"));
            } else {
                // Check amounts
                Invoice local = localMap.get(key);
                if (remote.montoTotal().compareTo(local.getTotalAmount()) != 0) {
                    discrepancies.add(new DiscrepancyItem(
                            String.valueOf(remote.tipoDte()),
                            remote.folio(),
                            remote.rutEmisor(),
                            remote.fechaEmision(),
                            remote.montoTotal(),
                            local.getTotalAmount(),
                            "AMOUNT_MISMATCH",
                            "CRITICAL"));
                }
            }
        }

        // Check Local vs SII (Missing in SII?)
        for (Invoice local : localInvoices) {
            // Only care about valid DTEs
            String key = generateKey(local);
            if (!siiMap.containsKey(key)) {
                discrepancies.add(new DiscrepancyItem(
                        String.valueOf(local.getType().getCode()),
                        local.getFolio(),
                        local.getIssuerRut(), // Note: Logic on counterpart depends on Sales vs Purchase
                        local.getDate(),
                        BigDecimal.ZERO,
                        local.getTotalAmount(),
                        "MISSING_IN_SII",
                        "CRITICAL"));
            }
        }

        // 4. Summaries
        TaxSummary siiTotal = calculateSiiSummary(fullSiiList);
        TaxSummary erpTotal = calculateErpSummary(localInvoices);

        boolean match = discrepancies.isEmpty() &&
                siiTotal.netAmount().compareTo(erpTotal.netAmount()) == 0 &&
                siiTotal.iva().compareTo(erpTotal.iva()) == 0;

        return new SiiAuditReportDTO(siiTotal, erpTotal, discrepancies, match);
    }

    private String generateKey(Invoice inv) {
        // Key format: Type-Folio-IssuerRut
        // Assuming issuer identifies the doc unique alongside type and folio globally?
        // Yes, Folio is per issuer per type.
        return inv.getType().getCode() + "-" + inv.getFolio() + "-" + inv.getIssuerRut();
    }

    private String generateKey(RcvData rcv) {
        return rcv.tipoDte() + "-" + rcv.folio() + "-" + rcv.rutEmisor();
    }

    private TaxSummary calculateSiiSummary(List<RcvData> list) {
        BigDecimal net = list.stream().map(RcvData::montoNeto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal iva = list.stream().map(RcvData::montoIva).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TaxSummary(net, iva, list.size());
    }

    private TaxSummary calculateErpSummary(List<Invoice> list) {
        BigDecimal net = list.stream().map(Invoice::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal iva = list.stream().map(Invoice::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TaxSummary(net, iva, list.size());
    }
}
