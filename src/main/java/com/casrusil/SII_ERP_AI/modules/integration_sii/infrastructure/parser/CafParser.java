package com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.parser;

import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.exception.SiiParsingException;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.Caf;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Parser para archivos CAF (Código de Autorización de Folios).
 * 
 * <p>
 * Procesa el archivo XML entregado por el SII que contiene los folios
 * autorizados
 * y la clave privada para firmar el timbre electrónico (TED).
 * 
 * <p>
 * Extrae información crítica como:
 * <ul>
 * <li>Rango de folios autorizados (Desde-Hasta).</li>
 * <li>RUT de la empresa emisora.</li>
 * <li>Clave privada RSA para firma del TED.</li>
 * </ul>
 * 
 * @since 1.0
 */
@Component
public class CafParser {

    public Caf parse(String xmlContent) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlContent)));

            Element cafElement = (Element) doc.getElementsByTagName("CAF").item(0);
            if (cafElement == null) {
                throw new SiiParsingException("XML does not contain CAF element");
            }

            Element daElement = (Element) cafElement.getElementsByTagName("DA").item(0);
            Element rngElement = (Element) daElement.getElementsByTagName("RNG").item(0);

            String tipoDte = getTagValue(daElement, "TD");
            Long rangoDesde = Long.parseLong(getTagValue(rngElement, "D"));
            Long rangoHasta = Long.parseLong(getTagValue(rngElement, "H"));

            String rsask = getTagValue(daElement, "RSASK");
            PrivateKey privateKey = parsePrivateKey(rsask);

            return new Caf(xmlContent, rangoDesde, rangoHasta, privateKey, tipoDte);

        } catch (Exception e) {
            throw new SiiParsingException("Error parsing CAF XML", e);
        }
    }

    private String getTagValue(Element element, String tagName) {
        return element.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private PrivateKey parsePrivateKey(String rsaskBase64) throws Exception {
        // Remove headers/footers if present (though usually CAF has raw base64)
        String cleanKey = rsaskBase64
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
