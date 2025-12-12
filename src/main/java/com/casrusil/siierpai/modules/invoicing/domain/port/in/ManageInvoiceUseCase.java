package com.casrusil.siierpai.modules.invoicing.domain.port.in;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para gestionar facturas electrónicas (DTEs).
 * 
 * <p>
 * Este contrato define las operaciones CRUD básicas para facturas,
 * incluyendo creación, consulta individual y listado. Las facturas pueden
 * provenir de sincronización con el SII o creación manual.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Crear nuevas facturas en el sistema</li>
 * <li>Consultar facturas por ID</li>
 * <li>Listar todas las facturas de la empresa actual</li>
 * <li>Publicar
 * {@link com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent}</li>
 * </ul>
 * 
 * <h2>Ejemplo de uso:</h2>
 * 
 * <pre>{@code
 * // Crear factura
 * Invoice invoice = Invoice.create(
 *         companyId,
 *         InvoiceType.FACTURA_AFECTA,
 *         12345L,
 *         "76.123.456-7",
 *         LocalDate.now(),
 *         new BigDecimal("100000"));
 * Invoice created = manageInvoiceUseCase.createInvoice(invoice);
 * 
 * // Consultar factura
 * Invoice found = manageInvoiceUseCase.getInvoice(created.getId());
 * }</pre>
 * 
 * @see Invoice
 * @see com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent
 * @since 1.0
 */
public interface ManageInvoiceUseCase {

    /**
     * Crea una nueva factura en el sistema.
     * 
     * <p>
     * Publica un
     * {@link com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent}
     * que dispara la generación automática de asientos contables.
     * 
     * @param invoice La factura a crear
     * @return La factura creada con ID asignado
     * @throws IllegalArgumentException si la factura es inválida
     */
    Invoice createInvoice(Invoice invoice);

    /**
     * Obtiene una factura por su ID.
     * 
     * @param id ID de la factura
     * @return La factura encontrada
     * @throws IllegalArgumentException si la factura no existe
     */
    Invoice getInvoice(UUID id);

    /**
     * Lista todas las facturas de la empresa actual.
     * 
     * <p>
     * Usa el
     * {@link com.casrusil.siierpai.shared.infrastructure.context.CompanyContext}
     * para filtrar automáticamente por empresa.
     * 
     * @return Lista de facturas de la empresa
     */
    List<Invoice> getInvoices();
}
