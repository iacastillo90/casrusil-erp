package com.casrusil.siierpai.modules.integration_sii.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.rest.SiiUploadClient;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.TedGenerator;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.XmlDsigSigner;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.xml.DteXmlBuilder;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.xml.EnvioDteBuilder;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DteSenderServiceTest {

    @Mock
    private XmlDsigSigner xmlDsigSigner;

    @Mock
    private SiiTokenRepository tokenRepository;

    @Mock
    private Pkcs12Handler pkcs12Handler;

    @Mock
    private CafRepository cafRepository;

    @Mock
    private TedGenerator tedGenerator;

    @Mock
    private DteXmlBuilder dteXmlBuilder;

    @Mock
    private EnvioDteBuilder envioDteBuilder;

    @Mock
    private SiiUploadClient siiUploadClient;

    @Mock
    private SiiCertificate siiCertificate;

    @Mock
    private X509Certificate x509Certificate;

    @Mock
    private PrivateKey privateKey;

    private DteSenderService dteSenderService;

    @BeforeEach
    void setUp() {
        dteSenderService = new DteSenderService(
                xmlDsigSigner,
                tokenRepository,
                pkcs12Handler,
                cafRepository,
                tedGenerator,
                dteXmlBuilder,
                envioDteBuilder,
                siiUploadClient);

        // Set default certificate path for testing
        ReflectionTestUtils.setField(dteSenderService, "defaultCertPath", "/test/cert.p12");
        ReflectionTestUtils.setField(dteSenderService, "defaultCertPassword", "test-password");
    }

    @Test
    void shouldSendInvoiceSuccessfully() {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = createTestInvoice(companyId);

        SiiToken validToken = new SiiToken("test-token", Instant.now().plusSeconds(3600));

        when(tokenRepository.findByCompanyId(companyId)).thenReturn(Optional.of(validToken));
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);
        when(siiCertificate.certificate()).thenReturn(x509Certificate);
        when(siiCertificate.privateKey()).thenReturn(privateKey);

        // Mocks for new dependencies
        when(cafRepository.findActiveForFolio(any(), anyString(), anyLong()))
                .thenReturn(Optional.of(mock(com.casrusil.siierpai.modules.integration_sii.domain.model.Caf.class)));
        when(tedGenerator.generateTedXml(any(), any())).thenReturn("<TED>...</TED>");
        when(dteXmlBuilder.buildDte(any(), anyString())).thenReturn("<DTE>...</DTE>");
        when(envioDteBuilder.wrap(anyString(), any(), anyString(), anyString())).thenReturn("<EnvioDTE>...</EnvioDTE>");
        when(siiUploadClient.uploadEnvioDte(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("TRACK_ID_123");

        when(xmlDsigSigner.signXml(anyString(), anyString(), any(SiiCertificate.class)))
                .thenReturn("<SignedXML>...</SignedXML>");

        // When
        boolean result = dteSenderService.sendInvoice(invoice, companyId);

        // Then
        assertTrue(result);
        verify(tokenRepository).findByCompanyId(companyId);
        verify(pkcs12Handler).loadCertificate("/test/cert.p12", "test-password");
        verify(xmlDsigSigner, atLeastOnce()).signXml(anyString(), anyString(), eq(siiCertificate));
        verify(siiUploadClient).uploadEnvioDte(eq("test-token"), anyString(), anyString(), anyString());
    }

    @Test
    void shouldFailWhenTokenNotFound() {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = createTestInvoice(companyId);

        when(tokenRepository.findByCompanyId(companyId)).thenReturn(Optional.empty());
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);

        // Mocks for dependencies called before token check
        when(cafRepository.findActiveForFolio(any(), anyString(), anyLong()))
                .thenReturn(Optional.of(mock(com.casrusil.siierpai.modules.integration_sii.domain.model.Caf.class)));
        when(tedGenerator.generateTedXml(any(), any())).thenReturn("<TED>...</TED>");
        when(dteXmlBuilder.buildDte(any(), anyString())).thenReturn("<DTE>...</DTE>");
        when(envioDteBuilder.wrap(anyString(), any(), anyString(), anyString())).thenReturn("<EnvioDTE>...</EnvioDTE>");
        when(xmlDsigSigner.signXml(anyString(), anyString(), any(SiiCertificate.class)))
                .thenReturn("<SignedXML>...</SignedXML>");

        // When
        boolean result = dteSenderService.sendInvoice(invoice, companyId);

        // Then
        assertFalse(result);
        verify(tokenRepository).findByCompanyId(companyId);
    }

    @Test
    void shouldFailWhenTokenExpired() {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = createTestInvoice(companyId);

        SiiToken expiredToken = new SiiToken("expired-token", Instant.now().minusSeconds(3600));

        when(tokenRepository.findByCompanyId(companyId)).thenReturn(Optional.of(expiredToken));
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);

        // Mocks for dependencies called before token check
        when(cafRepository.findActiveForFolio(any(), anyString(), anyLong()))
                .thenReturn(Optional.of(mock(com.casrusil.siierpai.modules.integration_sii.domain.model.Caf.class)));
        when(tedGenerator.generateTedXml(any(), any())).thenReturn("<TED>...</TED>");
        when(dteXmlBuilder.buildDte(any(), anyString())).thenReturn("<DTE>...</DTE>");
        when(envioDteBuilder.wrap(anyString(), any(), anyString(), anyString())).thenReturn("<EnvioDTE>...</EnvioDTE>");
        when(xmlDsigSigner.signXml(anyString(), anyString(), any(SiiCertificate.class)))
                .thenReturn("<SignedXML>...</SignedXML>");

        // When
        boolean result = dteSenderService.sendInvoice(invoice, companyId);

        // Then
        assertFalse(result);
        verify(tokenRepository).findByCompanyId(companyId);
    }

    @Test
    void shouldGenerateValidDteXml() {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = createTestInvoice(companyId);

        SiiToken validToken = new SiiToken("test-token", Instant.now().plusSeconds(3600));

        when(tokenRepository.findByCompanyId(companyId)).thenReturn(Optional.of(validToken));
        when(pkcs12Handler.loadCertificate(anyString(), anyString())).thenReturn(siiCertificate);
        when(siiCertificate.certificate()).thenReturn(x509Certificate);
        when(siiCertificate.privateKey()).thenReturn(privateKey);

        when(cafRepository.findActiveForFolio(any(), anyString(), anyLong()))
                .thenReturn(Optional.of(mock(com.casrusil.siierpai.modules.integration_sii.domain.model.Caf.class)));
        when(tedGenerator.generateTedXml(any(), any())).thenReturn("<TED>...</TED>");
        when(dteXmlBuilder.buildDte(any(), anyString())).thenReturn("<DTE version=\"1.0\">...</DTE>");
        when(envioDteBuilder.wrap(anyString(), any(), anyString(), anyString())).thenReturn("<EnvioDTE>...</EnvioDTE>");
        when(siiUploadClient.uploadEnvioDte(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("TRACK_ID_123");

        when(xmlDsigSigner.signXml(anyString(), anyString(), any(SiiCertificate.class)))
                .thenReturn("<SignedXML>...</SignedXML>");

        // When
        boolean result = dteSenderService.sendInvoice(invoice, companyId);

        // Then
        assertTrue(result);
        verify(dteXmlBuilder).buildDte(eq(invoice), anyString());
    }

    private Invoice createTestInvoice(CompanyId companyId) {
        return Invoice.create(
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76123456-7",
                "76987654-3",
                LocalDate.now(),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                Collections.emptyList());
    }
}
