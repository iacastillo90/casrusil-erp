package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DataCleanupService {

    private final AccountingEntryRepository entryRepository;
    private final com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository invoiceRepository;

    public DataCleanupService(AccountingEntryRepository entryRepository,
            com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository invoiceRepository) {
        this.entryRepository = entryRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Removes all entries of type 'INVOICE' for November 2025.
     * Also removes the source Invoices to allow re-import.
     * This fixes the "InvoiceAlreadyExistsException" when re-uploading the RCV.
     */
    @Transactional
    public void cleanupNovemberRcv() {
        var companyId = CompanyContext.requireCompanyId();

        // Define cleanup scope: November 1st to November 30th, 2025
        LocalDate start = LocalDate.of(2025, 11, 1);
        LocalDate end = LocalDate.of(2025, 11, 30);

        // 1. Delete accounting entries (The result)
        // Delete only transactional invoices, preserving Opening Balance
        entryRepository.deleteByReferenceTypeAndPeriod(companyId, "INVOICE", start, end);

        // 2. Delete the invoices (The source)
        // This is crucial to allow the InvoiceImportController to accept the file again
        invoiceRepository.deleteInPeriod(companyId, start, end);
    }
}
