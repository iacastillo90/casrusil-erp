package com.casrusil.SII_ERP_AI.modules.invoicing.domain.model;

/**
 * Tipos de Documentos Tributarios Electrónicos (DTE) soportados por el SII de
 * Chile.
 * 
 * <p>
 * Cada tipo de DTE tiene un código numérico oficial asignado por el SII.
 * Este enum mapea los códigos a nombres descriptivos y facilita la validación.
 * 
 * <h2>Tipos principales:</h2>
 * <ul>
 * <li><b>33</b> - Factura Electrónica (con IVA)</li>
 * <li><b>34</b> - Factura Exenta (sin IVA)</li>
 * <li><b>56</b> - Nota de Débito</li>
 * <li><b>61</b> - Nota de Crédito</li>
 * <li><b>52</b> - Guía de Despacho</li>
 * <li><b>110-112</b> - Documentos de exportación</li>
 * </ul>
 * 
 * <h2>Uso:</h2>
 * 
 * <pre>{@code
 * InvoiceType type = InvoiceType.FACTURA_ELECTRONICA;
 * int code = type.getCode(); // 33
 * InvoiceType fromCode = InvoiceType.fromCode(33);
 * }</pre>
 * 
 * @see com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice
 * @since 1.0
 */
public enum InvoiceType {
    /** Factura Electrónica (33) */
    FACTURA_ELECTRONICA(33),
    /** Factura No Afecta o Exenta Electrónica (34) */
    FACTURA_NO_AFECTA_O_EXENTA_ELECTRONICA(34),
    /** Liquidación Factura Electrónica (43) */
    LIQUIDACION_FACTURA_ELECTRONICA(43),
    /** Factura de Compra Electrónica (46) */
    FACTURA_COMPRA_ELECTRONICA(46),
    /** Guía de Despacho Electrónica (52) */
    GUIA_DESPACHO_ELECTRONICA(52),
    /** Nota de Débito Electrónica (56) */
    NOTA_DEBITO_ELECTRONICA(56),
    /** Nota de Crédito Electrónica (61) */
    NOTA_CREDITO_ELECTRONICA(61),
    /** Factura de Exportación Electrónica (110) */
    FACTURA_EXPORTACION_ELECTRONICA(110),
    /** Nota de Débito de Exportación Electrónica (111) */
    NOTA_DEBITO_EXPORTACION_ELECTRONICA(111),
    /** Nota de Crédito de Exportación Electrónica (112) */
    NOTA_CREDITO_EXPORTACION_ELECTRONICA(112);

    private final int code;

    InvoiceType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Obtiene el tipo de factura a partir de su código numérico.
     *
     * @param code El código del tipo de documento.
     * @return El InvoiceType correspondiente.
     * @throws IllegalArgumentException si el código no es válido.
     */
    public static InvoiceType fromCode(int code) {
        for (InvoiceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid InvoiceType code: " + code);
    }
}
