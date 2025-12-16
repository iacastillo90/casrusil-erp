package com.casrusil.siierpai.modules.invoicing.domain.port.out;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para persistencia de facturas electrónicas (DTEs).
 * 
 * <p>
 * Define el contrato de persistencia para entidades {@link Invoice}. Las
 * facturas
 * pueden provenir de sincronización con el SII o creación manual.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Persistir y recuperar facturas</li>
 * <li>Filtrar facturas por empresa (multi-tenancy)</li>
 * <li>Detectar duplicados (folio + RUT emisor)</li>
 * </ul>
 * 
 * <h2>Multi-tenancy:</h2>
 * <p>
 * Todas las consultas deben filtrar por {@code companyId} para garantizar
 * aislamiento de datos entre empresas.
 * 
 * @see Invoice
 * @see com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.out.persistence.InvoiceJpaAdapter
 * @since 1.0
 */
public interface InvoiceRepository {

        /**
         * Persiste una factura (crear o actualizar).
         * 
         * @param invoice La factura a persistir
         * @return La factura persistida con ID asignado
         */
        Invoice save(Invoice invoice);

        /**
         * Busca una factura por su ID.
         * 
         * @param id ID de la factura
         * @return Optional con la factura si existe, vacío si no
         */
        Optional<Invoice> findById(UUID id);

        /**
         * Lista todas las facturas de una empresa.
         * 
         * <p>
         * Usado por el
         * {@link com.casrusil.siierpai.modules.ai_assistant.application.tools.SearchInvoicesTool}
         * y servicios de análisis financiero.
         * 
         * @param companyId ID de la empresa
         * @return Lista de facturas de la empresa
         */
        List<Invoice> findByCompanyId(CompanyId companyId);

        /**
         * Verifica si existe una factura con el folio y RUT emisor especificados.
         * 
         * <p>
         * Usado por
         * {@link com.casrusil.siierpai.modules.accounting.application.service.DuplicateInvoiceDetector}
         * para prevenir duplicados durante la sincronización con el SII.
         * 
         * @param companyId ID de la empresa
         * @param folio     Número de folio de la factura
         * @param issuerRut RUT del emisor
         * @return true si existe, false si no
         */
        boolean existsByCompanyIdAndTypeCodeAndFolioAndIssuerRut(CompanyId companyId, Integer typeCode, Long folio,
                        String issuerRut);

        /**
         * Encuentra facturas pendientes de pago (abiertas).
         */
        default List<Invoice> findOpenInvoices(CompanyId companyId) {
                // Default implementation filtering in memory if JPA method not directly mapped
                // Ideally this would be a proper query method
                return findByCompanyId(companyId).stream()
                                .filter(invoice -> invoice
                                                .getStatus() == com.casrusil.siierpai.modules.invoicing.domain.model.PaymentStatus.PENDING)
                                .toList();
        }

        /**
         * Delete invoices within a date range.
         * Used for data cleanup.
         */
        void deleteInPeriod(CompanyId companyId, java.time.LocalDate start, java.time.LocalDate end);
}
