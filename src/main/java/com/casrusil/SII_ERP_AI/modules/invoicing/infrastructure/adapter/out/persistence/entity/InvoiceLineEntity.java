package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class InvoiceLineEntity {

    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;

    public InvoiceLineEntity() {
    }

    public InvoiceLineEntity(String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalAmount) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
