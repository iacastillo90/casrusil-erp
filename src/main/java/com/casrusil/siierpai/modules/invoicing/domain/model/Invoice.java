package com.casrusil.siierpai.modules.invoicing.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entidad raíz del agregado Invoice (Factura Electrónica / DTE).
 * 
 * <p>
 * Representa un Documento Tributario Electrónico según normativa del SII de
 * Chile.
 * Puede ser una factura emitida o recibida, con diferentes tipos (Afecta,
 * Exenta, etc.).
 * 
 * <h2>Invariantes:</h2>
 * <ul>
 * <li>El folio debe ser único por tipo de DTE y RUT emisor</li>
 * <li>Total = Neto + IVA (para facturas afectas)</li>
 * <li>El RUT emisor y receptor deben tener formato válido</li>
 * <li>La fecha no puede ser futura</li>
 * <li>Los montos deben ser positivos</li>
 * </ul>
 * 
 * <h2>Tipos de DTE soportados:</h2>
 * <ul>
 * <li>Factura Afecta (33) - Con IVA</li>
 * <li>Factura Exenta (34) - Sin IVA</li>
 * <li>Nota de Crédito (61)</li>
 * <li>Nota de Débito (56)</li>
 * </ul>
 * 
 * <h2>Ciclo de vida:</h2>
 * <ol>
 * <li>Creación:
 * {@link #create(CompanyId, InvoiceType, Long, String, String, LocalDate, BigDecimal, BigDecimal, BigDecimal, List)}</li>
 * <li>Publicación de evento:
 * {@link com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent}</li>
 * <li>Generación automática de asiento contable</li>
 * </ol>
 * 
 * <h2>Eventos del dominio:</h2>
 * <ul>
 * <li>{@link com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent}
 * - Al crear factura</li>
 * </ul>
 * 
 * @see InvoiceType
 * @see InvoiceLine
 * @see com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent
 * @since 1.0
 */
public class Invoice {
    public static final String ORIGIN_SII = "SII_SYNC";
    public static final String ORIGIN_MANUAL = "MANUAL_IMPORT";

    private final UUID id;
    private final CompanyId companyId;
    private final InvoiceType type;
    private final Long folio;
    private final String issuerRut;
    private final String receiverRut;
    private final LocalDate date;
    private final BigDecimal netAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal totalAmount;
    private final String origin;
    private final TransactionType transactionType;
    private final String businessName;
    private final BigDecimal fixedAssetAmount;
    private final BigDecimal commonUseVatAmount;
    private final List<InvoiceLine> items;

    public Invoice(UUID id, CompanyId companyId, InvoiceType type, Long folio, String issuerRut, String receiverRut,
            String businessName, LocalDate date, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount,
            BigDecimal fixedAssetAmount, BigDecimal commonUseVatAmount, String origin,
            TransactionType transactionType, List<InvoiceLine> items) {
        this.id = id;
        this.companyId = companyId;
        this.type = type;
        this.folio = folio;
        this.issuerRut = issuerRut;
        this.receiverRut = receiverRut;
        this.businessName = businessName;
        this.date = date;
        this.netAmount = netAmount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.fixedAssetAmount = fixedAssetAmount != null ? fixedAssetAmount : BigDecimal.ZERO;
        this.commonUseVatAmount = commonUseVatAmount != null ? commonUseVatAmount : BigDecimal.ZERO;
        this.origin = origin != null ? origin : ORIGIN_SII;
        this.transactionType = transactionType;
        this.items = items != null ? List.copyOf(items) : Collections.emptyList();
    }

    /**
     * Crea una nueva factura.
     * 
     * @param companyId   ID de la empresa emisora/receptora
     * @param type        Tipo de documento (Factura Afecta, Exenta, etc.)
     * @param folio       Folio del documento
     * @param issuerRut   RUT del emisor
     * @param receiverRut RUT del receptor
     * @param date        Fecha de emisión
     * @param netAmount   Monto neto
     * @param taxAmount   Monto IVA
     * @param totalAmount Monto total
     * @param items       Líneas de detalle
     * @return Nueva instancia de Invoice con origen SII_SYNC por defecto
     */
    public static Invoice create(CompanyId companyId, InvoiceType type, Long folio, String issuerRut,
            String receiverRut,
            LocalDate date, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount,
            List<InvoiceLine> items) {
        return new Invoice(UUID.randomUUID(), companyId, type, folio, issuerRut, receiverRut, null, date, netAmount,
                taxAmount, totalAmount, BigDecimal.ZERO, BigDecimal.ZERO, ORIGIN_SII, TransactionType.SALE, items);
    }

    /**
     * Crea una nueva factura con origen específico (Legacy/Simple).
     */
    public static Invoice create(CompanyId companyId, InvoiceType type, Long folio, String issuerRut,
            String receiverRut,
            LocalDate date, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount, String origin,
            TransactionType transactionType, List<InvoiceLine> items) {
        return new Invoice(UUID.randomUUID(), companyId, type, folio, issuerRut, receiverRut, null, date, netAmount,
                taxAmount, totalAmount, BigDecimal.ZERO, BigDecimal.ZERO, origin, transactionType, items);
    }

    /**
     * Crea una nueva factura completa con metadatos SII.
     */
    public static Invoice create(CompanyId companyId, InvoiceType type, Long folio, String issuerRut,
            String receiverRut, String businessName,
            LocalDate date, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount,
            BigDecimal fixedAssetAmount, BigDecimal commonUseVatAmount,
            String origin,
            TransactionType transactionType, List<InvoiceLine> items) {
        return new Invoice(UUID.randomUUID(), companyId, type, folio, issuerRut, receiverRut, businessName, date,
                netAmount,
                taxAmount, totalAmount, fixedAssetAmount, commonUseVatAmount, origin, transactionType, items);
    }

    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public InvoiceType getType() {
        return type;
    }

    public Long getFolio() {
        return folio;
    }

    public String getIssuerRut() {
        return issuerRut;
    }

    public String getReceiverRut() {
        return receiverRut;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getBusinessName() {
        return businessName;
    }

    public BigDecimal getFixedAssetAmount() {
        return fixedAssetAmount;
    }

    public BigDecimal getCommonUseVatAmount() {
        return commonUseVatAmount;
    }

    public String getOrigin() {
        return origin;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public List<InvoiceLine> getItems() {
        return items;
    }
}
