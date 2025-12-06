package com.casrusil.SII_ERP_AI.shared.domain.exception;

/**
 * Excepción base para todas las excepciones de negocio del dominio.
 * 
 * <p>
 * Todas las excepciones específicas del dominio (ej:
 * {@code InvoiceNotFoundException},
 * {@code InvalidInvoiceException}) deben extender de esta clase. Esto permite
 * capturarlas
 * de forma genérica en los controladores o manejadores de errores globales.
 * 
 * <h2>Uso:</h2>
 * 
 * <pre>{@code
 * public class InsufficientFundsException extends DomainException {
 *     public InsufficientFundsException(String accountId) {
 *         super("Insufficient funds in account: " + accountId);
 *     }
 * }
 * }</pre>
 * 
 * @since 1.0
 */
public abstract class DomainException extends RuntimeException {
    /**
     * Crea una nueva excepción de dominio con un mensaje descriptivo.
     * 
     * @param message Descripción del error de negocio.
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de dominio con un mensaje y la causa original.
     * 
     * @param message Descripción del error de negocio.
     * @param cause   La excepción original que causó este error (si existe).
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
