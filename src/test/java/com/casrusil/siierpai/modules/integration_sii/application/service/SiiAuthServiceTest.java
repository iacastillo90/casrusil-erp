package com.casrusil.siierpai.modules.integration_sii.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiSoapPort;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.XmlDsigSigner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiiAuthServiceTest {

    @Mock
    private SiiSoapPort siiSoapPort;

    @Mock
    private XmlDsigSigner xmlDsigSigner;

    @InjectMocks
    private SiiAuthService siiAuthService;

    @Test
    void shouldAuthenticateSuccessfully() {
        // Given
        String seed = "1234567890";
        String signedSeed = "<signed>1234567890</signed>";
        String tokenValue = "TOKEN123";
        SiiCertificate certificate = org.mockito.Mockito.mock(SiiCertificate.class);

        when(siiSoapPort.getSeed()).thenReturn(seed);
        when(xmlDsigSigner.signXml(any(), eq(""), eq(certificate))).thenReturn(signedSeed);
        when(siiSoapPort.getToken(signedSeed)).thenReturn(tokenValue);

        // When
        SiiToken token = siiAuthService.authenticate(certificate);

        // Then
        assertNotNull(token);
        assertEquals(tokenValue, token.token());
        assertTrue(token.isValid());
    }
}
