package com.casrusil.siierpai.modules.integration_sii.infrastructure.scheduler;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.AuthenticateSiiUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Scheduled task to refresh SII authentication tokens for all active companies.
 * Runs every 55 minutes to ensure tokens are refreshed before 1-hour
 * expiration.
 */
@Component
public class TokenRefreshScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TokenRefreshScheduler.class);

    private final AuthenticateSiiUseCase authenticateSiiUseCase;
    private final SiiTokenRepository tokenRepository;
    private final CompanyRepository companyRepository;
    private final Pkcs12Handler pkcs12Handler;

    @Value("${sii.certificate.path:}")
    private String defaultCertPath;

    @Value("${sii.certificate.password:}")
    private String defaultCertPassword;

    public TokenRefreshScheduler(
            AuthenticateSiiUseCase authenticateSiiUseCase,
            SiiTokenRepository tokenRepository,
            CompanyRepository companyRepository,
            Pkcs12Handler pkcs12Handler) {
        this.authenticateSiiUseCase = authenticateSiiUseCase;
        this.tokenRepository = tokenRepository;
        this.companyRepository = companyRepository;
        this.pkcs12Handler = pkcs12Handler;
    }

    /**
     * Refresh tokens for all active companies.
     * Runs every 55 minutes (3300000 ms) to refresh before 1-hour expiration.
     */
    @Scheduled(fixedRate = 3300000, initialDelay = 60000) // 55 min, start after 1 min
    public void refreshTokens() {
        logger.info("Starting SII token refresh for all companies...");

        try {
            List<Company> activeCompanies = companyRepository.findAll();
            logger.info("Found {} companies to refresh tokens", activeCompanies.size());

            int successCount = 0;
            int failureCount = 0;

            for (Company company : activeCompanies) {
                if (!company.isActive()) {
                    logger.debug("Skipping inactive company: {}", company.getId());
                    continue;
                }

                try {
                    refreshTokenForCompany(company.getId());
                    successCount++;
                } catch (Exception e) {
                    logger.error("Failed to refresh token for company {}: {}",
                            company.getId(), e.getMessage(), e);
                    failureCount++;
                }
            }

            logger.info("Token refresh completed. Success: {}, Failures: {}",
                    successCount, failureCount);

        } catch (Exception e) {
            logger.error("Error during token refresh scheduler execution: {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Refresh token for a specific company.
     */
    private void refreshTokenForCompany(CompanyId companyId) {
        // Check if token exists and is still valid
        Optional<SiiToken> existingToken = tokenRepository.findByCompanyId(companyId);
        if (existingToken.isPresent() && existingToken.get().isValid()) {
            logger.debug("Token for company {} is still valid, skipping refresh", companyId);
            return;
        }

        logger.info("Refreshing token for company: {}", companyId);

        // Load certificate for company
        // For MVP, using default certificate. In production, each company would have
        // their own.
        SiiCertificate certificate = loadCertificateForCompany(companyId);

        // Authenticate and get new token
        SiiToken newToken = authenticateSiiUseCase.authenticate(certificate);

        // Store token
        tokenRepository.save(companyId, newToken);

        logger.info("Token refreshed successfully for company: {}", companyId);
    }

    /**
     * Load SII certificate for a company.
     * For MVP, uses default certificate. In production, load company-specific cert.
     */
    private SiiCertificate loadCertificateForCompany(CompanyId companyId) {
        // TODO: In production, load company-specific certificate from database or
        // secure storage
        if (defaultCertPath == null || defaultCertPath.isEmpty()) {
            throw new IllegalStateException(
                    "No SII certificate configured. Set sii.certificate.path in application.properties");
        }

        return pkcs12Handler.loadCertificate(defaultCertPath, defaultCertPassword);
    }

    /**
     * Manual trigger for token refresh (for testing or admin operations).
     */
    public void refreshAllTokensNow() {
        logger.info("Manual token refresh triggered");
        refreshTokens();
    }
}
