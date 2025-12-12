package com.casrusil.siierpai.modules.invoicing.application.service;

import com.casrusil.siierpai.modules.invoicing.domain.exception.InvalidInvoiceException;
import com.casrusil.siierpai.modules.invoicing.domain.exception.InvoiceAlreadyExistsException;
import com.casrusil.siierpai.modules.invoicing.domain.exception.InvoiceNotFoundException;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.ManageInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.exception.DomainException;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación centralizado para la gestión de facturas.
 * 
 * <p>
 * Implementa múltiples interfaces de uso (Create, Manage, Search) para actuar
 * como fachada única para todas las operaciones relacionadas con facturas.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Creación y validación de nuevas facturas.</li>
 * <li>Búsqueda y recuperación de facturas existentes.</li>
 * <li>Garantizar el aislamiento multi-tenant en todas las consultas.</li>
 * <li>Prevención de duplicados.</li>
 * </ul>
 * 
 * @see CreateInvoiceUseCase
 * @see SearchInvoicesUseCase
 * @see Invoice
 * @since 1.0
 */
import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class InvoiceManagementService implements ManageInvoiceUseCase, CreateInvoiceUseCase, SearchInvoicesUseCase {

    private final InvoiceRepository invoiceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InvoiceManagementService(InvoiceRepository invoiceRepository, ApplicationEventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        CompanyId companyId = CompanyContext.requireCompanyId();

        if (!invoice.getCompanyId().equals(companyId)) {
            throw new InvalidInvoiceException("Invoice company ID does not match current context");
        }

        if (invoiceRepository.existsByCompanyIdAndTypeCodeAndFolioAndIssuerRut(
                companyId, invoice.getType().getCode(), invoice.getFolio(), invoice.getIssuerRut())) {
            throw new InvoiceAlreadyExistsException("Invoice already exists: " + invoice.getFolio());
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        eventPublisher.publishEvent(new InvoiceCreatedEvent(savedInvoice));
        return savedInvoice;
    }

    @Override
    public Invoice getInvoice(UUID id) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return invoiceRepository.findById(id)
                .filter(invoice -> invoice.getCompanyId().equals(companyId))
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    }

    @Override
    public List<Invoice> getInvoices() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return invoiceRepository.findByCompanyId(companyId);
    }

    @Override
    public List<Invoice> getInvoicesByCompany(CompanyId companyId) {
        return invoiceRepository.findByCompanyId(companyId);
    }
}
