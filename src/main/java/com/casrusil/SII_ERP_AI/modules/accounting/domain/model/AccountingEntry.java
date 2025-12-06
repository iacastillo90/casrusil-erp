package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;

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
    private final Instant occurredOn;
    private final String description;
    private final String referenceId;
    private final String referenceType;
    private final List<AccountingEntryLine> lines;
    private final EntryType type;

    /**
     * Crea un nuevo asiento contable.
     * Valida automáticamente que el asiento esté balanceado (Debe == Haber).
     * 
     * @param companyId     ID de la empresa
     * @param description   Descripción del asiento
     * @param referenceId   ID de referencia (ej. ID de factura)
     * @param referenceType Tipo de referencia (ej. "INVOICE")
     * @param lines         Lista de líneas del asiento
     * @param type          Tipo de asiento
     * @throws IllegalArgumentException Si el asiento no está balanceado
     */
    public AccountingEntry(CompanyId companyId, String description, String referenceId, String referenceType,
            List<AccountingEntryLine> lines, EntryType type) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.occurredOn = Instant.now();
        this.description = description;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
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

    public CompanyId getCompanyId() {
        return companyId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
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

    public List<AccountingEntryLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public EntryType getType() {
        return type;
    }
}
