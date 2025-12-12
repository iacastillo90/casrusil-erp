package com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad para persistir el cálculo de huella de carbono asociado a una
 * factura.
 * Alcance 3 (Emisiones indirectas de la cadena de valor).
 */
@Entity
@Table(name = "sustainability_records", schema = "sustainability")
public class SustainabilityRecordEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false, unique = true)
    private InvoiceEntity invoice;

    @Column(name = "carbon_footprint_kg", nullable = false)
    private BigDecimal carbonFootprintKg;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public SustainabilityRecordEntity() {
    }

    public SustainabilityRecordEntity(UUID id, InvoiceEntity invoice, BigDecimal carbonFootprintKg, String categoryName,
            Double confidenceScore) {
        this.id = id;
        this.invoice = invoice;
        this.carbonFootprintKg = carbonFootprintKg;
        this.categoryName = categoryName;
        this.confidenceScore = confidenceScore;
        this.calculatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InvoiceEntity getInvoice() {
        return invoice;
    }

    public void setInvoice(InvoiceEntity invoice) {
        this.invoice = invoice;
    }

    public BigDecimal getCarbonFootprintKg() {
        return carbonFootprintKg;
    }

    public void setCarbonFootprintKg(BigDecimal carbonFootprintKg) {
        this.carbonFootprintKg = carbonFootprintKg;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
