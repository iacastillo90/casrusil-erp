package com.casrusil.siierpai.modules.integration_sii.infrastructure.xml;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Constructor de XML para Documentos Tributarios Electrónicos (DTE).
 * 
 * <p>
 * Genera la estructura XML estándar requerida por el SII para facturas,
 * notas de crédito y notas de débito.
 * 
 * <p>
 * Responsabilidades:
 * <ul>
 * <li>Estructurar el encabezado (IdDoc, Emisor, Receptor).</li>
 * <li>Detallar los ítems y totales.</li>
 * <li>Generar y adjuntar el Timbre Electrónico (TED).</li>
 * <li>Firmar digitalmente el documento.</li>
 * </ul>
 * 
 * @see XmlDsigSigner
 * @see TedGenerator
 * @since 1.0
 */
@Component
public class DteXmlBuilder {

    public String buildDte(Invoice invoice, String tedXml) {
        // This is a simplified DTE structure. In a real scenario, this would be much
        // more complex
        // and likely use JAXB or a template engine.
        // For now, we construct it manually to ensure exact control over the structure
        // for signing.

        StringBuilder sb = new StringBuilder();
        sb.append("<DTE version=\"1.0\">");

        sb.append("<Receptor>");
        sb.append("<RUTRecep>").append(invoice.getReceiverRut()).append("</RUTRecep>");
        // ... other Receptor fields
        sb.append("</Receptor>");

        sb.append("<Totales>");
        sb.append("<MntNeto>").append(invoice.getNetAmount()).append("</MntNeto>");
        sb.append("<TasaIVA>").append("19").append("</TasaIVA>");
        sb.append("<IVA>").append(invoice.getTaxAmount()).append("</IVA>");
        sb.append("<MntTotal>").append(invoice.getTotalAmount()).append("</MntTotal>");
        sb.append("</Totales>");
        sb.append("</Encabezado>");

        // Detalle (Simplified)
        sb.append("<Detalle>");
        sb.append("<NroLinDet>1</NroLinDet>");
        sb.append("<NmbItem>Servicios Profesionales</NmbItem>");
        sb.append("<MontoItem>").append(invoice.getNetAmount()).append("</MontoItem>");
        sb.append("</Detalle>");

        // Inject TED
        sb.append(tedXml);

        // Timestamp
        sb.append("<TmstFirma>").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .append("</TmstFirma>");

        sb.append("</Documento>");
        sb.append("</DTE>");

        return sb.toString();
    }
}
