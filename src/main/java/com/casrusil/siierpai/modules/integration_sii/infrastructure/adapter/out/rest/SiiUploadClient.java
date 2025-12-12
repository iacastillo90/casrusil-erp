package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.nio.charset.StandardCharsets;

@Component
public class SiiUploadClient {

    private final HttpClient httpClient;

    @Value("${sii.url.upload:https://palena.sii.cl/cgi_dte/UPL/DTEUpload}")
    private String uploadUrl;

    public SiiUploadClient() {
        this.httpClient = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
    }

    public String uploadEnvioDte(String token, String envioDteXml, String rutEmisor, String rutEmpresa) {
        // El SII requiere un formato Multipart específico con headers precisos
        String boundary = "---boundary" + System.currentTimeMillis();

        // Construir el cuerpo Multipart a mano
        String body = buildMultipartBody(boundary, envioDteXml, rutEmisor, rutEmpresa);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("User-Agent", "Mozilla/4.0 (compatible; PROG 1.0; Windows NT 5.0; YComp 5.0.2.4)")
                .header("Cookie", "TOKEN=" + token) // Autenticación vía Cookie
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("SII Upload failed with status: " + response.statusCode());
            }

            // Parsear respuesta para obtener el Track ID
            return extractTrackId(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Error subiendo DTE al SII", e);
        }
    }

    private String buildMultipartBody(String boundary, String xmlContent, String rutEmisor, String rutEmpresa) {
        String filename = "envio_" + System.currentTimeMillis() + ".xml";
        StringBuilder sb = new StringBuilder();

        // Part 1: rutSender
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"rutSender\"\r\n\r\n");
        sb.append(rutEmisor.split("-")[0]).append("\r\n");

        // Part 2: dvSender
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"dvSender\"\r\n\r\n");
        sb.append(rutEmisor.split("-")[1]).append("\r\n");

        // Part 3: rutCompany
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"rutCompany\"\r\n\r\n");
        sb.append(rutEmpresa.split("-")[0]).append("\r\n");

        // Part 4: dvCompany
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"dvCompany\"\r\n\r\n");
        sb.append(rutEmpresa.split("-")[1]).append("\r\n");

        // Part 5: archivo
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"").append(filename).append("\"\r\n");
        sb.append("Content-Type: text/xml\r\n\r\n");
        sb.append(xmlContent).append("\r\n");

        // End boundary
        sb.append("--").append(boundary).append("--\r\n");

        return sb.toString();
    }

    private String extractTrackId(String responseBody) {
        // SII Response format:
        // <RECEPCIONDTE>
        // <STATUS>0</STATUS>
        // <TRACKID>1234567890</TRACKID>
        // </RECEPCIONDTE>

        try {
            int start = responseBody.indexOf("<TRACKID>");
            int end = responseBody.indexOf("</TRACKID>");
            if (start != -1 && end != -1) {
                return responseBody.substring(start + 9, end);
            }
            throw new RuntimeException("TrackID not found in response: " + responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing TrackID", e);
        }
    }
}
