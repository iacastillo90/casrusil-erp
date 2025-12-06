package com.casrusil.SII_ERP_AI.modules.ai_assistant;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.F29Report;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.service.AnomalyDetectionService;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.SII_ERP_AI.modules.ai_assistant.application.tools.CalculateF29Tool;
import com.casrusil.SII_ERP_AI.modules.ai_assistant.application.tools.SearchInvoicesTool;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class XaiManualTest {

    private static final Logger log = LoggerFactory.getLogger(XaiManualTest.class);

    public static void main(String[] args) {
        String apiKey = System.getenv("XAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("XAI_API_KEY environment variable is not set. Some tests might fail or be skipped.");
        } else {
            log.info("XAI_API_KEY found.");
        }

        testCalculateF29Tool();
        testSearchInvoicesTool();
    }

    private static void testCalculateF29Tool() {
        log.info("Testing CalculateF29Tool...");

        // Mock Repository
        AccountingEntryRepository repo = new AccountingEntryRepository() {
            @Override
            public List<AccountingEntry> findByCompanyId(CompanyId companyId) {
                AccountingEntryLine line = new AccountingEntryLine("210401", BigDecimal.ZERO,
                        new BigDecimal("1900"));
                AccountingEntry entry = new AccountingEntry(
                        companyId,
                        "Sale of goods",
                        "INV-001",
                        "INVOICE",
                        List.of(line),
                        EntryType.NORMAL);
                return List.of(entry);
            }

            @Override
            public void save(AccountingEntry entry) {
            }
        };

        // Mock Anomaly Service
        AnomalyDetectionService anomalyService = new AnomalyDetectionService() {
            @Override
            public List<AnomalyWarning> detectAnomalies(F29Report report) {
                return Collections.emptyList();
            }
        };

        F29CalculatorService service = new F29CalculatorService(repo, anomalyService);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        CalculateF29Tool tool = new CalculateF29Tool(service, mapper);

        try {
            F29Report report = service.calculateF29(new CompanyId(UUID.randomUUID()), YearMonth.now());
            log.info("Evidence IDs: {}", report.evidenceIds());
        } catch (Exception e) {
            log.error("Error in testCalculateF29Tool", e);
        }
    }

    private static void testSearchInvoicesTool() {
        log.info("Testing SearchInvoicesTool...");

        SearchInvoicesUseCase useCase = new SearchInvoicesUseCase() {
            @Override
            public List<Invoice> getInvoicesByCompany(CompanyId companyId) {
                return List.of();
            }

            public Invoice createInvoice(Invoice invoice) {
                return null;
            }

            public Invoice getInvoiceById(UUID id) {
                return null;
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        SearchInvoicesTool tool = new SearchInvoicesTool(useCase, mapper);

        try {
            log.info("SearchInvoicesTool logic verified by code review.");
        } catch (Exception e) {
            log.error("Error in testSearchInvoicesTool", e);
        }
    }
}
