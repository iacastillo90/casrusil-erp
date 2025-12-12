package com.casrusil.siierpai.modules.invoicing.domain.model;

import com.casrusil.siierpai.modules.invoicing.domain.exception.InvalidInvoiceException;

import java.math.BigDecimal;

/**
 * Línea de detalle de una factura electrónica (DTE).
 * 
 * <p>
 * Representa un ítem individual dentro de una factura. Es un Value Object
 * inmutable
 * implementado como Java Record que valida sus datos al momento de creación.
 * 
 * <h2>Invariantes:</h2>
 * <ul>
 * <li>Número de línea debe ser positivo</li>
 * <li>Nombre del ítem no puede estar vacío</li>
 * <li>Cantidad debe ser positiva</li>
 * <li>Precio no puede ser negativo</li>
 * <li>Monto = Cantidad × Precio</li>
 * </ul>
 * 
 * @param lineNumber Número de línea (orden en la factura)
 * @param itemName   Nombre o descripción del producto/servicio
 * @param itemCode   Código del producto (opcional)
 * @param quantity   Cantidad de unidades
 * @param price      Precio unitario
 * @param amount     Monto total de la línea (cantidad × precio)
 * @param unit       Unidad de medida (ej: "UN", "KG", "HR")
 * @see com.casrusil.siierpai.modules.invoicing.domain.model.Invoice
 * @since 1.0
 */
public record InvoiceLine(
        Integer lineNumber,
        String itemName,
        String itemCode,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal amount,
        String unit) {

    public InvoiceLine {
        if (lineNumber == null || lineNumber <= 0) {
            throw new InvalidInvoiceException("Line number must be positive");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new InvalidInvoiceException("Item name cannot be empty");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInvoiceException("Quantity must be positive");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInvoiceException("Price cannot be negative");
        }
    }
}
