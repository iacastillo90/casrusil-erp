package com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Generador de Timbre Electrónico DTE (TED).
 * 
 * <p>
 * Crea la estructura XML del TED (Tag DD) que contiene los datos críticos
 * del documento firmados con la clave privada del CAF.
 * 
 * <p>
 * El TED es obligatorio para la validez tributaria del DTE y su representación
 * gráfica (código de barras PDF417).
 * 
 * @see CafParser
 * @since 1.0
 */
@Component
public class TedGenerator {

    public String generateTedXml(Invoice invoice, Caf caf) {
        try {
            // 1. Construir la cadena de datos "Datos del Timbre" (DD)
            String datosTimbre = buildDD(invoice, caf);

            // 2. Firmar los datos usando la llave privada del CAF (SHA1withRSA)
            byte[] firma = sign(datosTimbre, caf.privateKey());

            // 3. Construir el XML final <TED>...
            // Note: The indentation and newlines here are important for some parsers,
            // but SII validates the signature against the content of DD.
            return String.format("""
                    <TED version="1.0">
                    <DD>
                    %s
                    </DD>
                    <FRMT algoritmo="SHA1withRSA">%s</FRMT>
                    </TED>""", datosTimbre, Base64.getEncoder().encodeToString(firma));

        } catch (Exception e) {
            throw new RuntimeException("Error generating TED", e);
        }
    }

    private String buildDD(Invoice invoice, Caf caf) {
        // Format:
        // <RE>...</RE><TD>...</TD><F>...</F><FE>...</FE><RR>...</RR><RSR>...</RSR><MNT>...</MNT><IT1>...</IT1><CAF
        // ...>...</CAF><TSTED>...</TSTED>
        // Critical: The order of tags MUST be exact.

        StringBuilder sb = new StringBuilder();
        sb.append("<RE>").append(invoice.getIssuerRut()).append("</RE>");
        sb.append("<TD>").append(invoice.getType().getCode()).append("</TD>");
        sb.append("<F>").append(invoice.getFolio()).append("</F>");
        sb.append("<FE>").append(invoice.getDate().format(DateTimeFormatter.ISO_DATE)).append("</FE>");
        sb.append("<RR>").append("66666666-6").append("</RR>"); // TODO: Receiver RUT
        sb.append("<RSR>").append("RECEIVER NAME").append("</RSR>"); // TODO: Receiver Name
        sb.append("<MNT>").append(invoice.getTotalAmount()).append("</MNT>");

        // Item 1 Detail (Optional but recommended to include at least one)
        sb.append("<IT1>").append("Detalle Factura").append("</IT1>");

        // CAF content (The <CAF> tag from the authorized XML)
        // We need to extract the <CAF>...</CAF> block from the full XML
        String cafBlock = extractCafBlock(caf.xmlContent());
        sb.append(cafBlock);

        // Timestamp
        sb.append("<TSTED>").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .append("</TSTED>");

        return sb.toString();
    }

    private byte[] sign(String data, java.security.PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.ISO_8859_1)); // SII uses ISO-8859-1
        return signature.sign();
    }

    private String extractCafBlock(String fullXml) {
        int start = fullXml.indexOf("<CAF");
        int end = fullXml.indexOf("</CAF>") + 6;
        if (start == -1 || end == -1) {
            throw new RuntimeException("Invalid CAF XML: Missing <CAF> tag");
        }
        return fullXml.substring(start, end);
    }
}
