package com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.adapter.out.soap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SiiAuthSoapClient {

    private final HttpClient httpClient;
    private final String siiUrlAuth;

    public SiiAuthSoapClient(@Value("${sii.url.auth:https://maullin.sii.cl/DTEWS}") String siiUrlAuth) {
        this(siiUrlAuth, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    protected SiiAuthSoapClient() {
        this.siiUrlAuth = null;
        this.httpClient = null;
    }

    public SiiAuthSoapClient(String siiUrlAuth, HttpClient httpClient) {
        this.siiUrlAuth = siiUrlAuth;
        this.httpClient = httpClient;
    }

    public String getSeed() {
        String soapBody = """
                <soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:def="http://DefaultNamespace">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <def:getSeed soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
                   </soapenv:Body>
                </soapenv:Envelope>
                """;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(siiUrlAuth + "/CrSeed.jws"))
                    .header("Content-Type", "text/xml;charset=UTF-8")
                    .header("SOAPAction", "")
                    .POST(HttpRequest.BodyPublishers.ofString(soapBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get seed. Status: " + response.statusCode());
            }

            return extractTagValue(response.body(), "SEMILLA");

        } catch (Exception e) {
            throw new RuntimeException("Error getting seed from SII", e);
        }
    }

    public String getToken(String signedSeed) {
        String soapBody = String.format(
                """
                        <soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:def="http://DefaultNamespace">
                           <soapenv:Header/>
                           <soapenv:Body>
                              <def:getToken soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                                 <pszXml xsi:type="xsd:string">%s</pszXml>
                              </def:getToken>
                           </soapenv:Body>
                        </soapenv:Envelope>
                        """,
                escapeXml(signedSeed));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(siiUrlAuth + "/GetTokenFromSeed.jws"))
                    .header("Content-Type", "text/xml;charset=UTF-8")
                    .header("SOAPAction", "")
                    .POST(HttpRequest.BodyPublishers.ofString(soapBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get token. Status: " + response.statusCode());
            }

            return extractTagValue(response.body(), "TOKEN");

        } catch (Exception e) {
            throw new RuntimeException("Error getting token from SII", e);
        }
    }

    private String extractTagValue(String xml, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">");
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("Tag " + tagName + " not found in response: " + xml);
    }

    private String escapeXml(String data) {
        return data.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
