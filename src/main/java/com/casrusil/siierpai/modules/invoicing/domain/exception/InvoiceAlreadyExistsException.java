package com.casrusil.siierpai.modules.invoicing.domain.exception;

import com.casrusil.siierpai.shared.domain.exception.DomainException;

/**
 * Excepción lanzada al intentar crear una factura que ya existe.
 * 
 * <p>
 * Se valida por la combinación única de Emisor + Tipo + Folio.
 * 
 * @since 1.0
 */
public class InvoiceAlreadyExistsException extends DomainException {
    public InvoiceAlreadyExistsException(String message) {
        super(message);
    }
}
