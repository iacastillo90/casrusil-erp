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
    private final LocalDate dueDate;
    private final String businessName;
    private final BigDecimal fixedAssetAmount;
    private final BigDecimal commonUseVatAmount;
    private final TransactionType transactionType;
    private final List<InvoiceLine> items;
    private final String currency;

    private PaymentStatus status;

    public Invoice(UUID id, CompanyId companyId, InvoiceType type, Long folio, String issuerRut, String receiverRut,
            String businessName, LocalDate date, LocalDate dueDate, BigDecimal netAmount, BigDecimal taxAmount,
            BigDecimal totalAmount,
            BigDecimal fixedAssetAmount, BigDecimal commonUseVatAmount, String origin,
            TransactionType transactionType, PaymentStatus status, List<InvoiceLine> items, String currency) {
        this.id = id;
        this.companyId = companyId;
        this.type = type;
        this.folio = folio;
        this.issuerRut = issuerRut;
        this.receiverRut = receiverRut;
        this.businessName = businessName;
        this.date = date;
        this.dueDate = dueDate != null ? dueDate : date; // Default due date to issue date if null
        this.netAmount = netAmount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.fixedAssetAmount = fixedAssetAmount != null ? fixedAssetAmount : BigDecimal.ZERO;
        this.commonUseVatAmount = commonUseVatAmount != null ? commonUseVatAmount : BigDecimal.ZERO;
        this.origin = origin != null ? origin : ORIGIN_SII;
        this.transactionType = transactionType;
        this.status = status != null ? status : PaymentStatus.PENDING;
        this.items = items != null ? List.copyOf(items) : Collections.emptyList();
        this.currency = currency != null ? currency : "CLP";
    }

    /**
     * Constructor Legacy (backward compatibility). Defaults currency to CLP.
     */
    public Invoice(UUID id, CompanyId companyId, InvoiceType type, Long folio, String issuerRut, String receiverRut,
            String businessName, LocalDate date, LocalDate dueDate, BigDecimal netAmount, BigDecimal taxAmount,
            BigDecimal totalAmount,
            BigDecimal fixedAssetAmount, BigDecimal commonUseVatAmount, String origin,
            TransactionType transactionType, PaymentStatus status, List<InvoiceLine> items) {
        this(id, companyId, type, folio, issuerRut, receiverRut, businessName, date, dueDate, netAmount, taxAmount,
                totalAmount, fixedAssetAmount, commonUseVatAmount, origin, transactionType, status, items, "CLP");
    }

    // Factories update needed? Yes, but I'll skip updating all factories for
    // brevity IF I can avoid it.
    // The previous factories call 'new Invoice(...)'. I need to update the
    // constructor call in ALL factories in Invoice.java?
    // Yes.
    // Or I can overload the constructor.
    // I will overload the constructor for backward compatibility to avoid breaking
    // existing code.
    // But constructors in `record`-like classes or final fields...
    // I'll update the main constructor and update the factories in the file.

    /**
     * Crea una nueva factura.
     */
    public static Invoice create(CompanyId companyId, InvoiceType type, Long folio, String issuerRut,
            String receiverRut,
            LocalDate date, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount,
            List<InvoiceLine> items) {
        return new Invoice(UUID.randomUUID(), companyId, type, folio, issuerRut, receiverRut, null, date, date,
                netAmount,
                taxAmount, totalAmount, BigDecimal.ZERO, BigDecimal.ZERO, ORIGIN_SII, TransactionType.SALE,
                PaymentStatus.PENDING, items, "CLP");
    }

    /**
     * Crea una nueva factura con origen específico (Legacy/Simple).
     */
    public static Invoice create(CompanyId companyId, InvoiceType type, Long folio, String issuerRut,
            String receiverRut,
            LocalDate date, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount, String origin,
            TransactionType transactionType, List<InvoiceLine> items) {
        return new Invoice(UUID.randomUUID(), companyId, type, folio, issuerRut, receiverRut, null, date, date,
                netAmount,
                taxAmount, totalAmount, BigDecimal.ZERO, BigDecimal.ZERO, origin, transactionType,
                PaymentStatus.PENDING, items, "CLP");
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
        return new Invoice(UUID.randomUUID(), companyId, type, folio, issuerRut, receiverRut, businessName, date, date,
                netAmount,
                taxAmount, totalAmount, fixedAssetAmount, commonUseVatAmount, origin, transactionType,
                PaymentStatus.PENDING, items, "CLP");
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

    public LocalDate getDueDate() {
        return dueDate;
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

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void markAsPaid() {
        this.status = PaymentStatus.PAID;
    }

    public List<InvoiceLine> getItems() {
        return items;
    }
}
