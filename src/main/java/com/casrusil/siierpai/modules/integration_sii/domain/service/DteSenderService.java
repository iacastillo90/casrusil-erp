package com.casrusil.siierpai.modules.integration_sii.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.XmlDsigSigner;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import com.casrusil.siierpai.modules.sso.domain.exception.CertificateNotFoundException;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyCertificateRepository;
import java.util.Optional;
import java.io.ByteArrayInputStream;

/**
 * Service for sending DTEs (Electronic Tax Documents) to SII.
 * Handles XML generation, signing, and SOAP transmission.
 */
@Service
public class DteSenderService {

    private static final Logger logger = LoggerFactory.getLogger(DteSenderService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final XmlDsigSigner xmlDsigSigner;
    private final SiiTokenRepository tokenRepository;
    private final Pkcs12Handler pkcs12Handler;
    private final com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository cafRepository;
    private final com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.TedGenerator tedGenerator;
    private final com.casrusil.siierpai.modules.integration_sii.infrastructure.xml.DteXmlBuilder dteXmlBuilder;
    private final com.casrusil.siierpai.modules.integration_sii.infrastructure.xml.EnvioDteBuilder envioDteBuilder;
    @Value("${sii.certificate.path:}")
    private String defaultCertPath;

    @Value("${sii.certificate.password:}")
    private String defaultCertPassword;

    private final com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.rest.SiiUploadClient siiUploadClient;
    private final com.casrusil.siierpai.modules.sso.domain.port.out.CompanyCertificateRepository certificateRepository;

    public DteSenderService(
            XmlDsigSigner xmlDsigSigner,
            SiiTokenRepository tokenRepository,
            Pkcs12Handler pkcs12Handler,
            com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository cafRepository,
            com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.TedGenerator tedGenerator,
            com.casrusil.siierpai.modules.integration_sii.infrastructure.xml.DteXmlBuilder dteXmlBuilder,
            com.casrusil.siierpai.modules.integration_sii.infrastructure.xml.EnvioDteBuilder envioDteBuilder,
            com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.rest.SiiUploadClient siiUploadClient,
            com.casrusil.siierpai.modules.sso.domain.port.out.CompanyCertificateRepository certificateRepository) {
        this.xmlDsigSigner = xmlDsigSigner;
        this.tokenRepository = tokenRepository;
        this.pkcs12Handler = pkcs12Handler;
        this.cafRepository = cafRepository;
        this.tedGenerator = tedGenerator;
        this.dteXmlBuilder = dteXmlBuilder;
        this.envioDteBuilder = envioDteBuilder;
        this.siiUploadClient = siiUploadClient;
        this.certificateRepository = certificateRepository;
    }

    /**
     * Send an invoice to SII.
     * 
     * @param invoice   The invoice to send
     * @param companyId The company ID
     * @return true if sent successfully, false otherwise
     */
    public boolean sendInvoice(Invoice invoice, CompanyId companyId) {
        logger.info("Sending Invoice #{} to SII for company {}", invoice.getFolio(), companyId);

        try {
            // 1. Get CAF for this DTE type and Folio
            String tipoDteStr = String.valueOf(invoice.getType().getCode());
            com.casrusil.siierpai.modules.integration_sii.domain.model.Caf caf = cafRepository
                    .findActiveForFolio(companyId, tipoDteStr, invoice.getFolio())
                    .orElseThrow(() -> new IllegalStateException(
                            "No active CAF found for DTE " + tipoDteStr + " Folio " + invoice.getFolio()));

            // 2. Generate TED
            String tedXml = tedGenerator.generateTedXml(invoice, caf);

            // 3. Build DTE XML (Injecting TED)
            String dteXml = dteXmlBuilder.buildDte(invoice, tedXml);

            // 4. Sign DTE (Individual Signature)
            SiiCertificate certificate = loadCertificateForCompany(companyId);
            String dteId = "DTE_" + invoice.getFolio(); // Must match ID in DteXmlBuilder
            String signedDteXml = xmlDsigSigner.signXml(dteXml, dteId, certificate);

            // 5. Wrap in EnvioDTE
            // Extract RUTs from invoice (real data from SII)
            String rutEmisor = invoice.getIssuerRut(); // RUT del emisor (quien emite la factura)
            String rutEmpresa = invoice.getIssuerRut(); // RUT de la empresa (usualmente el mismo)
            String envioXml = envioDteBuilder.wrap(signedDteXml, companyId, rutEmisor, rutEmpresa);

            // 6. Sign EnvioDTE (SetDoc Signature)
            String signedEnvioXml = xmlDsigSigner.signXml(envioXml, "SetDoc", certificate);

            // 7. Get SII Token
            SiiToken token = tokenRepository.findByCompanyId(companyId)
                    .orElseThrow(() -> new IllegalStateException(
                            "No SII token found for company " + companyId + ". Please authenticate first."));

            if (!token.isValid()) {
                throw new IllegalStateException(
                        "SII token expired for company " + companyId + ". Token will be refreshed automatically.");
            }

            // 8. Upload to SII
            String trackId = siiUploadClient.uploadEnvioDte(token.token(), signedEnvioXml, rutEmisor, rutEmpresa);

            logger.info("Invoice #{} sent successfully. Track ID: {}", invoice.getFolio(), trackId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send invoice #{} to SII: {}", invoice.getFolio(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Load SII certificate for a company.
     * 
     * Prioritize DB storage. Fallback to properties if allowed/configured.
     * 
     * @param companyId The company ID
     * @return SiiCertificate loaded
     * @throws com.casrusil.siierpai.modules.sso.domain.exception.CertificateNotFoundException if
     *                                                                                         not
     *                                                                                         found
     */
    private SiiCertificate loadCertificateForCompany(CompanyId companyId) {
        // 1. Try DB
        java.util.Optional<com.casrusil.siierpai.modules.sso.domain.model.CompanyCertificate> certOpt = certificateRepository
                .findByCompanyId(companyId.value());

        if (certOpt.isPresent()) {
            com.casrusil.siierpai.modules.sso.domain.model.CompanyCertificate cert = certOpt.get();
            try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(cert.getCertificateData())) {
                return pkcs12Handler.loadCertificate(bis, cert.getPassword());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load certificate from DB for company " + companyId, e);
            }
        }

        // 2. Fallback to properties (MVP)
        if (defaultCertPath != null && !defaultCertPath.isEmpty()) {
            logger.warn("Using default fallback certificate for company {} (Not configured in DB)", companyId);
            return pkcs12Handler.loadCertificate(defaultCertPath, defaultCertPassword);
        }

        throw new com.casrusil.siierpai.modules.sso.domain.exception.CertificateNotFoundException(
                "No SII certificate found for company " + companyId);
    }
}
