package com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Componente de Firma Digital XML (XML-DSig).
 * 
 * <p>
 * Implementa el estándar XML Digital Signature para firmar documentos
 * electrónicos según la normativa del SII.
 * 
 * <p>
 * Características:
 * <ul>
 * <li>Firma Enveloped (la firma queda dentro del documento).</li>
 * <li>Uso de algoritmos RSA-SHA1 (estándar SII).</li>
 * <li>Canonicalización y transformación de referencias.</li>
 * </ul>
 * 
 * @see Pkcs12Handler
 * @since 1.0
 */
@Component
public class XmlDsigSigner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XmlDsigSigner.class);

    public XmlDsigSigner() {
        log.debug("XmlDsigSigner instantiated");
    }

    static {
        try {
            Init.init();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(XmlDsigSigner.class).error("Failed to initialize xmlsec", e);
        }
    }

    public String signXml(String xmlContent, String referenceId, SiiCertificate certificate) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlContent)));

            XMLSignature signature = new XMLSignature(doc, null, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);

            // Append signature to document root
            doc.getDocumentElement().appendChild(signature.getElement());

            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);

            String uri = (referenceId == null || referenceId.isEmpty()) ? "" : "#" + referenceId;
            signature.addDocument(uri, transforms,
                    org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1);

            signature.addKeyInfo(certificate.certificate());
            signature.addKeyInfo(certificate.certificate().getPublicKey());

            signature.sign(certificate.privateKey());

            return documentToString(doc);

        } catch (Exception e) {
            throw new RuntimeException("Error signing XML", e);
        }
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        // CRITICAL: SII requires ISO-8859-1
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "ISO-8859-1");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(baos));
        return baos.toString("ISO-8859-1");
    }
}
