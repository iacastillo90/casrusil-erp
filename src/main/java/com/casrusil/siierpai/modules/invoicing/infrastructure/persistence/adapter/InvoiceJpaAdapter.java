package com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceEntity;
import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.repository.InvoiceJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceItemEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para facturas.
 * 
 * <p>
 * Implementa {@link InvoiceRepository} para gestionar el ciclo de vida
 * de los documentos tributarios en la base de datos.
 * 
 * @see InvoiceRepository
 * @since 1.0
 */
@Component
@org.springframework.context.annotation.Primary
public class InvoiceJpaAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository invoiceJpaRepository;

    public InvoiceJpaAdapter(InvoiceJpaRepository invoiceJpaRepository) {
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = toEntity(invoice);
        InvoiceEntity savedEntity = invoiceJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return invoiceJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Invoice> findByCompanyId(CompanyId companyId) {
        return invoiceJpaRepository.findAllByCompanyId(companyId.value())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCompanyIdAndTypeCodeAndFolioAndIssuerRut(CompanyId companyId, Integer typeCode, Long folio,
            String issuerRut) {
        return invoiceJpaRepository.existsByCompanyIdAndTypeCodeAndFolioAndIssuerRut(companyId.value(), typeCode, folio,
                issuerRut);
    }

    private InvoiceEntity toEntity(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity(
                invoice.getId(),
                invoice.getCompanyId().value(),
                invoice.getType().getCode(),
                invoice.getFolio(),
                invoice.getIssuerRut(),
                invoice.getReceiverRut(),
                invoice.getBusinessName(),
                invoice.getDate(),
                invoice.getNetAmount(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getFixedAssetAmount(),
                invoice.getCommonUseVatAmount(),
                invoice.getOrigin(),
                invoice.getTransactionType());

        if (invoice.getItems() != null) {
            for (InvoiceLine line : invoice.getItems()) {
                InvoiceItemEntity itemEntity = new InvoiceItemEntity(
                        line.lineNumber(),
                        line.itemName(),
                        line.itemCode(),
                        line.quantity(),
                        line.price(),
                        line.amount(),
                        line.unit());
                entity.addItem(itemEntity);
            }
        }
        return entity;
    }

    private Invoice toDomain(InvoiceEntity entity) {
        List<InvoiceLine> items = Collections.emptyList();
        if (entity.getItems() != null) {
            items = entity.getItems().stream()
                    .map(item -> new InvoiceLine(
                            item.getLineNumber(),
                            item.getItemName(),
                            item.getItemCode(),
                            item.getQuantity(),
                            item.getPrice(),
                            item.getAmount(),
                            item.getUnit()))
                    .collect(Collectors.toList());
        }

        return new Invoice(
                entity.getId(),
                new CompanyId(entity.getCompanyId()),
                InvoiceType.fromCode(entity.getTypeCode()),
                entity.getFolio(),
                entity.getIssuerRut(),
                entity.getReceiverRut(),
                entity.getBusinessName(),
                entity.getDate(),
                entity.getNetAmount(),
                entity.getTaxAmount(),
                entity.getTotalAmount(),
                entity.getFixedAssetAmount(),
                entity.getCommonUseVatAmount(),
                entity.getOrigin(),
                entity.getTransactionType(),
                items);
    }
}
