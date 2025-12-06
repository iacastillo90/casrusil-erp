package com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.crypto;

import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.SiiCertificate;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Enumeration;

/**
 * Manejador de Certificados Digitales PKCS#12.
 * 
 * <p>
 * Responsable de cargar y gestionar el certificado digital del contribuyente,
 * necesario para la autenticación en el SII y la firma de documentos.
 * 
 * <p>
 * Funcionalidades:
 * <ul>
 * <li>Cargar keystore desde archivo .p12</li>
 * <li>Extraer clave privada y certificado X.509</li>
 * <li>Validar vigencia del certificado</li>
 * </ul>
 * 
 * @since 1.0
 */
@Component
public class Pkcs12Handler {

    public SiiCertificate loadCertificate(String path, String password) {
        try (InputStream is = new FileInputStream(path)) {
            return loadCertificate(is, password);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PKCS12 certificate from path: " + path, e);
        }
    }

    public SiiCertificate loadCertificate(InputStream is, String password) {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(is, password.toCharArray());

            Enumeration<String> aliases = keystore.aliases();
            String alias = null;
            while (aliases.hasMoreElements()) {
                alias = aliases.nextElement();
                if (keystore.isKeyEntry(alias)) {
                    break;
                }
            }

            if (alias == null) {
                throw new IllegalArgumentException("No key entry found in PKCS12 file");
            }

            PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, password.toCharArray());
            X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);

            // Extract RUT from Subject DN (CN=...) or other extension if needed
            // For now, we assume RUT is passed separately or extracted from CN if standard
            // format
            // Simplified for this implementation
            String rut = extractRutFromCert(cert);

            return new SiiCertificate(
                    cert,
                    privateKey,
                    rut,
                    cert.getNotBefore().toInstant(),
                    cert.getNotAfter().toInstant());

        } catch (Exception e) {
            throw new RuntimeException("Failed to load PKCS12 certificate", e);
        }
    }

    private String extractRutFromCert(X509Certificate cert) {
        // Implementation depends on specific CA format (e.g., Acepta, E-Sign)
        // Usually in CN or SerialNumber
        String subjectDN = cert.getSubjectX500Principal().getName();
        // Placeholder logic
        return "UNKNOWN-RUT";
    }
}
