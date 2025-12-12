package com.casrusil.siierpai.modules.integration_sii.infrastructure.scheduler;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.AuthenticateSiiUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRefreshSchedulerTest {

    @Mock
    private AuthenticateSiiUseCase authenticateSiiUseCase;

    @Mock
    private SiiTokenRepository tokenRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private Pkcs12Handler pkcs12Handler;

    @Mock
    private SiiCertificate siiCertificate;

    private TokenRefreshScheduler tokenRefreshScheduler;

    @BeforeEach
    void setUp() {
        tokenRefreshScheduler = new TokenRefreshScheduler(
                authenticateSiiUseCase,
                tokenRepository,
                companyRepository,
                pkcs12Handler);

        // Set certificate configuration
        ReflectionTestUtils.setField(tokenRefreshScheduler, "defaultCertPath", "/test/cert.p12");
        ReflectionTestUtils.setField(tokenRefreshScheduler, "defaultCertPassword", "test-password");
    }

    @Test
    void shouldRefreshTokensForAllActiveCompanies() {
        // Given
        Company activeCompany1 = createCompany(true);
        Company activeCompany2 = createCompany(true);
        List<Company> companies = Arrays.asList(activeCompany1, activeCompany2);

        SiiToken newToken = new SiiToken("new-token", Instant.now().plusSeconds(3600));

        when(companyRepository.findAll()).thenReturn(companies);
        when(tokenRepository.findByCompanyId(any(CompanyId.class))).thenReturn(Optional.empty());
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);
        when(authenticateSiiUseCase.authenticate(any(SiiCertificate.class))).thenReturn(newToken);

        // When
        tokenRefreshScheduler.refreshTokens();

        // Then
        verify(companyRepository).findAll();
        verify(authenticateSiiUseCase, times(2)).authenticate(siiCertificate);
        verify(tokenRepository, times(2)).save(any(CompanyId.class), eq(newToken));
    }

    @Test
    void shouldSkipInactiveCompanies() {
        // Given
        Company activeCompany = createCompany(true);
        Company inactiveCompany = createCompany(false);
        List<Company> companies = Arrays.asList(activeCompany, inactiveCompany);

        SiiToken newToken = new SiiToken("new-token", Instant.now().plusSeconds(3600));

        when(companyRepository.findAll()).thenReturn(companies);
        when(tokenRepository.findByCompanyId(any(CompanyId.class))).thenReturn(Optional.empty());
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);
        when(authenticateSiiUseCase.authenticate(any(SiiCertificate.class))).thenReturn(newToken);

        // When
        tokenRefreshScheduler.refreshTokens();

        // Then
        verify(authenticateSiiUseCase, times(1)).authenticate(siiCertificate);
        verify(tokenRepository, times(1)).save(any(CompanyId.class), eq(newToken));
    }

    @Test
    void shouldSkipRefreshIfTokenStillValid() {
        // Given
        Company activeCompany = createCompany(true);
        SiiToken validToken = new SiiToken("valid-token", Instant.now().plusSeconds(3600));

        when(companyRepository.findAll()).thenReturn(Collections.singletonList(activeCompany));
        when(tokenRepository.findByCompanyId(activeCompany.getId())).thenReturn(Optional.of(validToken));

        // When
        tokenRefreshScheduler.refreshTokens();

        // Then
        verify(authenticateSiiUseCase, never()).authenticate(any());
        verify(tokenRepository, never()).save(any(), any());
    }

    @Test
    void shouldRefreshExpiredToken() {
        // Given
        Company activeCompany = createCompany(true);
        SiiToken expiredToken = new SiiToken("expired-token", Instant.now().minusSeconds(3600));
        SiiToken newToken = new SiiToken("new-token", Instant.now().plusSeconds(3600));

        when(companyRepository.findAll()).thenReturn(Collections.singletonList(activeCompany));
        when(tokenRepository.findByCompanyId(activeCompany.getId())).thenReturn(Optional.of(expiredToken));
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);
        when(authenticateSiiUseCase.authenticate(siiCertificate)).thenReturn(newToken);

        // When
        tokenRefreshScheduler.refreshTokens();

        // Then
        verify(authenticateSiiUseCase).authenticate(siiCertificate);
        verify(tokenRepository).save(activeCompany.getId(), newToken);
    }

    @Test
    void shouldContinueOnFailureForOneCompany() {
        // Given
        Company company1 = createCompany(true);
        Company company2 = createCompany(true);
        List<Company> companies = Arrays.asList(company1, company2);

        SiiToken newToken = new SiiToken("new-token", Instant.now().plusSeconds(3600));

        when(companyRepository.findAll()).thenReturn(companies);
        when(tokenRepository.findByCompanyId(company1.getId())).thenReturn(Optional.empty());
        when(tokenRepository.findByCompanyId(company2.getId())).thenReturn(Optional.empty());
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);

        // First company fails, second succeeds
        when(authenticateSiiUseCase.authenticate(siiCertificate))
                .thenThrow(new RuntimeException("Authentication failed"))
                .thenReturn(newToken);

        // When
        tokenRefreshScheduler.refreshTokens();

        // Then
        verify(authenticateSiiUseCase, times(2)).authenticate(siiCertificate);
        verify(tokenRepository, times(1)).save(company2.getId(), newToken);
    }

    @Test
    void shouldHandleManualRefreshTrigger() {
        // Given
        Company activeCompany = createCompany(true);
        SiiToken newToken = new SiiToken("new-token", Instant.now().plusSeconds(3600));

        when(companyRepository.findAll()).thenReturn(Collections.singletonList(activeCompany));
        when(tokenRepository.findByCompanyId(any(CompanyId.class))).thenReturn(Optional.empty());
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);
        when(authenticateSiiUseCase.authenticate(siiCertificate)).thenReturn(newToken);

        // When
        tokenRefreshScheduler.refreshAllTokensNow();

        // Then
        verify(authenticateSiiUseCase).authenticate(siiCertificate);
        verify(tokenRepository).save(any(CompanyId.class), eq(newToken));
    }

    private Company createCompany(boolean active) {
        return new Company(
                new CompanyId(UUID.randomUUID()),
                "76123456-7",
                "Test Company",
                "test@company.cl",
                active,
                Instant.now());
    }
}
