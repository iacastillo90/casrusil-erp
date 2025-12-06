package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para facturas.
 * 
 * <p>
 * Representa la cabecera de un documento tributario (Factura, Nota de Crédito,
 * etc.).
 * Almacena los datos principales requeridos por el SII y para la gestión
 * interna.
 * 
 * <p>
 * Relaciones:
 * <ul>
 * <li>{@code items}: Lista de líneas de detalle de la factura.</li>
 * </ul>
 * 
 * @since 1.0
 */
@Entity
@Table(name = "invoices", schema = "invoicing")
public class InvoiceEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "type_code", nullable = false)
    private Integer typeCode;

    @Column(name = "folio", nullable = false)
    private Long folio;

    @Column(name = "issuer_rut", nullable = false)
    private String issuerRut;

    @Column(name = "receiver_rut", nullable = false)
    private String receiverRut;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "origin")
    private String origin;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType transactionType;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItemEntity> items = new ArrayList<>();

    public InvoiceEntity() {
    }

    public InvoiceEntity(UUID id, UUID companyId, Integer typeCode, Long folio, String issuerRut, String receiverRut,
            LocalDate date, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount, String origin,
            com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType transactionType) {
        this.id = id;
        this.companyId = companyId;
        this.typeCode = typeCode;
        this.folio = folio;
        this.issuerRut = issuerRut;
        this.receiverRut = receiverRut;
        this.date = date;
        this.netAmount = netAmount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.origin = origin;
        this.transactionType = transactionType;
    }

    public void addItem(InvoiceItemEntity item) {
        items.add(item);
        item.setInvoice(this);
    }

    public void removeItem(InvoiceItemEntity item) {
        items.remove(item);
        item.setInvoice(null);
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public Long getFolio() {
        return folio;
    }

    public String getIssuerRut() {
        return issuerRut;
    }

    public String getReceiverRut() {
        return receiverRut;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(
            com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public List<InvoiceItemEntity> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemEntity> items) {
        this.items = items;
    }
}
