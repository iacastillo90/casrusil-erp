package com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class XmlDsigSignerTest {

    private XmlDsigSigner xmlDsigSigner;

    @Mock
    private SiiCertificate siiCertificate;

    @Mock
    private X509Certificate x509Certificate;

    private PrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        xmlDsigSigner = new XmlDsigSigner();

        // Generate a real key pair for signing because XMLSignature needs a valid key
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = keyPair.getPrivate();

        // Mock SiiCertificate
        when(siiCertificate.privateKey()).thenReturn(privateKey);
        when(siiCertificate.certificate()).thenReturn(x509Certificate);
        when(x509Certificate.getPublicKey()).thenReturn(keyPair.getPublic());
    }

    @Test
    void signXml_ShouldSignDocumentCorrectly() throws Exception {
        String xmlContent = "<DTE version=\"1.0\"><Documento ID=\"DTE_123\"></Documento></DTE>";
        String referenceId = "DTE_123";

        String signedXml = xmlDsigSigner.signXml(xmlContent, referenceId, siiCertificate);

        assertNotNull(signedXml);
        assertTrue(signedXml.contains("Signature"));
        assertTrue(signedXml.contains("SignedInfo"));
        assertTrue(signedXml.contains("SignatureValue"));

        // Verify it's a valid XML
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(signedXml.getBytes("ISO-8859-1")));
        NodeList signatures = doc.getElementsByTagName("Signature");
        assertEquals(1, signatures.getLength());
    }
}
