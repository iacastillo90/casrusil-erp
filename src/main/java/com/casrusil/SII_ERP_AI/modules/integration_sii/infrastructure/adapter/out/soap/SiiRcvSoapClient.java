package com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.adapter.out.soap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class SiiRcvSoapClient {

    private final HttpClient httpClient;
    private final String siiUrlRcv;

    public SiiRcvSoapClient(
            @Value("${sii.url.rcv:https://www4.sii.cl/consulfaliinternetws/reporte/}") String siiUrlRcv) {
        this(siiUrlRcv, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }

    protected SiiRcvSoapClient() {
        this.siiUrlRcv = null;
        this.httpClient = null;
    }

    public SiiRcvSoapClient(String siiUrlRcv, HttpClient httpClient) {
        this.siiUrlRcv = siiUrlRcv;
        this.httpClient = httpClient;
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "sii")
    public String downloadRcv(String token, String rutEmpresa, String period, boolean isPurchase) {
        // Note: The actual endpoint and payload for RCV download varies.
        // This is a simplified implementation assuming a standard SOAP/REST hybrid
        // often used by SII.
        // For the purpose of this exercise, we'll simulate a request structure.

        String operation = isPurchase ? "COMPRA" : "VENTA";
        String soapBody = String.format(
                """
                        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:def="http://DefaultNamespace">
                           <soapenv:Header/>
                           <soapenv:Body>
                              <def:getEstDteAv>
                                 <Token>%s</Token>
                                 <RutEmpresa>%s</RutEmpresa>
                                 <Periodo>%s</Periodo>
                                 <Operacion>%s</Operacion>
                              </def:getEstDteAv>
                           </soapenv:Body>
                        </soapenv:Envelope>
                        """,
                token, rutEmpresa, period, operation);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(siiUrlRcv)) // URL might need adjustment based on real SII WSDL
                    .header("Content-Type", "text/xml;charset=UTF-8")
                    .header("SOAPAction", "")
                    .header("Cookie", "TOKEN=" + token) // Sometimes required in headers
                    .POST(HttpRequest.BodyPublishers.ofString(soapBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to download RCV. Status: " + response.statusCode());
            }

            return response.body();

        } catch (Exception e) {
            throw new RuntimeException("Error downloading RCV from SII", e);
        }
    }
}
