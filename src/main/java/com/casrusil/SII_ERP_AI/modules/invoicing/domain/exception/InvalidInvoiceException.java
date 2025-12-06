package com.casrusil.SII_ERP_AI.modules.invoicing.domain.exception;

import com.casrusil.SII_ERP_AI.shared.domain.exception.DomainException;

/**
 * Excepción lanzada cuando una factura no cumple las reglas de negocio.
 * 
 * <p>
 * Ejemplos: Montos negativos, RUT inválido, falta de ítems, etc.
 * 
 * @since 1.0
 */
public class InvalidInvoiceException extends DomainException {
    public InvalidInvoiceException(String message) {
        super(message);
    }
}
