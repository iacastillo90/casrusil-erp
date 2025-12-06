package com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.adapter.out.soap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SiiAuthSoapClientTest {

    private SiiAuthSoapClient siiAuthSoapClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        siiAuthSoapClient = new SiiAuthSoapClient("https://mock-sii-url.cl", httpClient);
    }

    @Test
    void getSeed_ShouldReturnSeed_WhenResponseIsValid() throws Exception {
        String mockResponseXml = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                   <soapenv:Body>
                      <ns:getSeedResponse xmlns:ns="http://DefaultNamespace">
                         <SEMILLA>1234567890</SEMILLA>
                      </ns:getSeedResponse>
                   </soapenv:Body>
                </soapenv:Envelope>
                """;

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockResponseXml);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        String seed = siiAuthSoapClient.getSeed();

        assertEquals("1234567890", seed);
    }

    @Test
    void getToken_ShouldReturnToken_WhenResponseIsValid() throws Exception {
        String mockResponseXml = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                   <soapenv:Body>
                      <ns:getTokenResponse xmlns:ns="http://DefaultNamespace">
                         <TOKEN>TOKEN123</TOKEN>
                      </ns:getTokenResponse>
                   </soapenv:Body>
                </soapenv:Envelope>
                """;

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockResponseXml);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        String token = siiAuthSoapClient.getToken("signedSeed");

        assertEquals("TOKEN123", token);
    }

    @Test
    void getSeed_ShouldThrowException_WhenResponseIsError() throws Exception {
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThrows(RuntimeException.class, () -> siiAuthSoapClient.getSeed());
    }
}
