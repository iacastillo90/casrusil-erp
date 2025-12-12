package com.casrusil.siierpai.modules.treasury.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CashTransaction {
    private final UUID id;
    private final CompanyId companyId;
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount; // Positive for IN, Negative for OUT
    private final String reference;
    private final String category; // e.g., "OFFICE_SUPPLIES", "SALES_CASH"

    public CashTransaction(UUID id, CompanyId companyId, LocalDate date, String description, BigDecimal amount,
            String reference, String category) {
        this.id = id;
        this.companyId = companyId;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.reference = reference;
        this.category = category;
    }

    public static CashTransaction create(CompanyId companyId, LocalDate date, String description, BigDecimal amount,
            String reference, String category) {
        return new CashTransaction(UUID.randomUUID(), companyId, date, description, amount, reference, category);
    }

    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public String getCategory() {
        return category;
    }
}
