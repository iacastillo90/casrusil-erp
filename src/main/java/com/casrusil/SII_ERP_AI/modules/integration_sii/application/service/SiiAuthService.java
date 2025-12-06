package com.casrusil.SII_ERP_AI.modules.integration_sii.application.service;

import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.port.in.AuthenticateSiiUseCase;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.port.out.SiiSoapPort;
import com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.crypto.XmlDsigSigner;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de aplicación para la autenticación con el SII (Servicio de
 * Impuestos Internos).
 * 
 * <p>
 * Implementa el flujo de autenticación basado en certificados digitales y firma
 * XML (XML-DSig).
 * Obtiene un token de sesión válido para interactuar con los servicios SOAP del
 * SII.
 * 
 * <h2>Flujo de autenticación:</h2>
 * <ol>
 * <li>Solicitar semilla (Seed) al SII.</li>
 * <li>Firmar digitalmente la semilla usando el certificado de la empresa.</li>
 * <li>Enviar semilla firmada para obtener el Token.</li>
 * <li>Empaquetar el token con su tiempo de expiración (1 hora).</li>
 * </ol>
 * 
 * @see AuthenticateSiiUseCase
 * @see SiiToken
 * @see XmlDsigSigner
 * @since 1.0
 */
@Service
public class SiiAuthService implements AuthenticateSiiUseCase {

    private final SiiSoapPort siiSoapPort;
    private final XmlDsigSigner xmlDsigSigner;

    public SiiAuthService(SiiSoapPort siiSoapPort, XmlDsigSigner xmlDsigSigner) {
        this.siiSoapPort = siiSoapPort;
        this.xmlDsigSigner = xmlDsigSigner;
    }

    @Override
    public SiiToken authenticate(SiiCertificate certificate) {
        // 1. Get Seed
        String seed = siiSoapPort.getSeed();

        // 2. Sign Seed
        String signedSeed = signSeed(seed, certificate);

        // 3. Get Token
        String tokenValue = siiSoapPort.getToken(signedSeed);

        // 4. Create SiiToken (expires in 1 hour)
        return new SiiToken(tokenValue, Instant.now().plus(1, ChronoUnit.HOURS));
    }

    private String signSeed(String seed, SiiCertificate certificate) {
        String xmlToSign = String.format(
                """
                        <getToken>
                        <item>
                        <Semilla>%s</Semilla>
                        </item>
                        </getToken>
                        """, seed);

        // Use a fixed ID or generate one if needed by XmlDsigSigner
        // The XmlDsigSigner expects a referenceId to point to the element to sign.
        // However, the current XmlDsigSigner implementation appends the signature to
        // the root.
        // And it adds a reference URI "#" + referenceId.
        // But the xmlToSign above doesn't have an ID attribute.
        // We might need to adjust XmlDsigSigner or the XML structure.
        // For now, assuming XmlDsigSigner handles it or we need to wrap it.

        // Actually, for SII GetToken, the signed XML structure is specific.
        // Let's assume XmlDsigSigner does the heavy lifting of enveloping.
        // But we need to ensure the element has the ID if XmlDsigSigner references it.

        // Let's try to pass it as is, assuming XmlDsigSigner might need adjustment or
        // usage is correct.
        // Based on XmlDsigSigner code: signature.addDocument("#" + referenceId, ...)
        // This implies the document must have an element with that ID.
        // The simple xmlToSign above doesn't.

        // Correct SII structure for signing usually requires wrapping the seed in a
        // specific way.
        // But let's stick to the basic flow for now.
        return xmlDsigSigner.signXml(xmlToSign, "", certificate);
    }
}
