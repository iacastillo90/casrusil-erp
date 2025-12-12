package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.soap;

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

class SiiRcvSoapClientTest {

    private SiiRcvSoapClient siiRcvSoapClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        siiRcvSoapClient = new SiiRcvSoapClient("https://mock-sii-rcv-url.cl", httpClient);
    }

    @Test
    void downloadRcv_ShouldReturnCsvContent_WhenResponseIsValid() throws Exception {
        String mockCsvContent = "RUT,FOLIO,FECHA,MONTO\n76123456-7,100,2023-10-01,1000";

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockCsvContent);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) httpResponse);

        String result = siiRcvSoapClient.downloadRcv("TOKEN123", "76123456-7", "202310", true);

        assertEquals(mockCsvContent, result);
    }

    @Test
    void downloadRcv_ShouldThrowException_WhenResponseIsError() throws Exception {
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) httpResponse);

        assertThrows(RuntimeException.class, () -> {
            siiRcvSoapClient.downloadRcv("TOKEN123", "76123456-7", "202310", true);
        });
    }
}
