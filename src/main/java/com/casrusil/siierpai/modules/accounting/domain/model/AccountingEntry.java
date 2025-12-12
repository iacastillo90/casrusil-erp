package com.casrusil.siierpai.modules.accounting.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Representa un asiento contable (Entidad de Dominio).
 * Agrupa múltiples líneas (débitos y créditos) y garantiza el principio de
 * partida doble.
 */
public class AccountingEntry {
    private final UUID id;
    private final CompanyId companyId;
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final java.time.LocalDate entryDate;
    private final String description;
    private final String referenceId;
    private final String referenceType;
    private final String taxPayerId;
    private final String taxPayerName;
    private final String documentType;
    private final String documentNumber;
    private final String status; // DRAFT, POSTED
    private final List<AccountingEntryLine> lines;
    private final EntryType type;

    /**
     * Crea un nuevo asiento contable.
     * Valida automáticamente que el asiento esté balanceado (Debe == Haber).
     * 
     * @param companyId      ID de la empresa
     * @param description    Descripción del asiento
     * @param referenceId    ID de referencia (ej. ID de factura)
     * @param referenceType  Tipo de referencia (ej. "INVOICE")
     * @param taxPayerId     RUT del contribuyente asociado
     * @param taxPayerName   Nombre del contribuyente
     * @param documentType   Tipo de documento (ej. "33")
     * @param documentNumber Número de documento (Folio)
     * @param status         Estado del asiento (DRAFT, POSTED)
     * @param lines          Lista de líneas del asiento
     * @param type           Tipo de asiento
     * @throws IllegalArgumentException Si el asiento no está balanceado
     */
    public AccountingEntry(CompanyId companyId, String description, String referenceId, String referenceType,
            String taxPayerId, String taxPayerName, String documentType, String documentNumber, String status,
            List<AccountingEntryLine> lines, EntryType type) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.entryDate = java.time.LocalDate.now();
        this.description = description;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.taxPayerId = taxPayerId;
        this.taxPayerName = taxPayerName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.status = status;
        this.lines = lines;
        this.type = type;
        validateDoubleEntry();
    }

    // Constructor for creation with specific date OR reconstruction
    public AccountingEntry(CompanyId companyId, java.time.LocalDate entryDate, String description, String referenceId,
            String referenceType, String taxPayerId, String taxPayerName, String documentType, String documentNumber,
            String status,
            List<AccountingEntryLine> lines, EntryType type) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.entryDate = entryDate;
        this.description = description;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.taxPayerId = taxPayerId;
        this.taxPayerName = taxPayerName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.status = status;
        this.lines = lines;
        this.type = type;
        validateDoubleEntry();
    }

    private void validateDoubleEntry() {
        BigDecimal totalDebit = lines.stream()
                .map(AccountingEntryLine::debit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream()
                .map(AccountingEntryLine::credit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException(
                    "Accounting entry is not balanced. Debit: " + totalDebit + ", Credit: " + totalCredit);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getShortId() {
        return id.toString().substring(0, 8);
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public java.time.LocalDate getEntryDate() {
        return entryDate;
    }

    public String getDescription() {
        return description;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getTaxPayerId() {
        return taxPayerId;
    }

    public String getTaxPayerName() {
        return taxPayerName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getStatus() {
        return status;
    }

    public List<AccountingEntryLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public EntryType getType() {
        return type;
    }
}
