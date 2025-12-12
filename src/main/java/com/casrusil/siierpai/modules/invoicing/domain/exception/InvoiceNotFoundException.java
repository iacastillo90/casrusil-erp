package com.casrusil.siierpai.modules.invoicing.domain.exception;

import com.casrusil.siierpai.shared.domain.exception.DomainException;

/**
 * Excepción lanzada cuando no se encuentra una factura solicitada.
 * 
 * <p>
 * Utilizada en búsquedas por ID o Folio.
 * 
 * @since 1.0
 */
public class InvoiceNotFoundException extends DomainException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}
