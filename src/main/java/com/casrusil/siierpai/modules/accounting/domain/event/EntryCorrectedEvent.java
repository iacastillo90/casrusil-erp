package com.casrusil.siierpai.modules.accounting.domain.event;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio que se dispara cuando un contador corrige manualmente
 * un asiento contable que fue generado automáticamente.
 * 
 * Este evento es la clave del "Feedback Loop" - permite al sistema aprender
 * de las correcciones del usuario.
 */
public class EntryCorrectedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final AccountingEntry originalEntry;
    private final AccountingEntry correctedEntry;
    private final UUID userId;
    private final String correctionReason;

    public EntryCorrectedEvent(AccountingEntry originalEntry, AccountingEntry correctedEntry,
            UUID userId, String correctionReason) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.originalEntry = originalEntry;
        this.correctedEntry = correctedEntry;
        this.userId = userId;
        this.correctionReason = correctionReason;
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    public AccountingEntry getOriginalEntry() {
        return originalEntry;
    }

    public AccountingEntry getCorrectedEntry() {
        return correctedEntry;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCorrectionReason() {
        return correctionReason;
    }

    /**
     * Extrae el cambio más significativo de la corrección.
     * Por ejemplo, si se cambió la cuenta contable de una línea.
     */
    public String extractMainChange() {
        // Comparar líneas del asiento original vs corregido
        if (originalEntry.getLines().size() != correctedEntry.getLines().size()) {
            return "Número de líneas modificado";
        }

        for (int i = 0; i < originalEntry.getLines().size(); i++) {
            var originalLine = originalEntry.getLines().get(i);
            var correctedLine = correctedEntry.getLines().get(i);

            if (!originalLine.accountCode().equals(correctedLine.accountCode())) {
                return String.format("Cuenta cambiada de %s a %s",
                        originalLine.accountCode(),
                        correctedLine.accountCode());
            }
        }

        return "Corrección menor";
    }
}
