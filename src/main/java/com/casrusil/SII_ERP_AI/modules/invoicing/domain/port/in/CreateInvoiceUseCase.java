package com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.in;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;

/**
 * Caso de uso para crear facturas electrónicas (DTEs).
 * 
 * <p>
 * Este contrato define la operación de creación de facturas, que puede
 * ser invocada tanto desde la sincronización con el SII como desde la
 * creación manual de documentos.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Validar datos de la factura antes de persistir</li>
 * <li>Asignar ID único a la factura</li>
 * <li>Publicar
 * {@link com.casrusil.SII_ERP_AI.modules.invoicing.domain.event.InvoiceCreatedEvent}</li>
 * <li>Disparar generación automática de asientos contables</li>
 * </ul>
 * 
 * <h2>Ejemplo de uso:</h2>
 * 
 * <pre>{@code
 * Invoice invoice = Invoice.create(
 *         companyId,
 *         InvoiceType.FACTURA_AFECTA,
 *         12345L,
 *         "76.123.456-7",
 *         "ACME Corp",
 *         LocalDate.now(),
 *         new BigDecimal("100000"),
 *         new BigDecimal("19000"),
 *         new BigDecimal("119000"));
 * Invoice created = createInvoiceUseCase.createInvoice(invoice);
 * }</pre>
 * 
 * @see Invoice
 * @see com.casrusil.SII_ERP_AI.modules.invoicing.domain.event.InvoiceCreatedEvent
 * @see ManageInvoiceUseCase
 * @since 1.0
 */
public interface CreateInvoiceUseCase {

    /**
     * Crea una nueva factura en el sistema.
     * 
     * <p>
     * Valida la factura, la persiste y publica un evento que dispara
     * la generación automática de asientos contables.
     * 
     * @param invoice La factura a crear (con todos los datos requeridos)
     * @return La factura creada con ID asignado
     * @throws IllegalArgumentException si la factura es inválida o ya existe
     * @see com.casrusil.SII_ERP_AI.modules.accounting.application.listener.InvoiceAccountingListener
     */
    Invoice createInvoice(Invoice invoice);
}
