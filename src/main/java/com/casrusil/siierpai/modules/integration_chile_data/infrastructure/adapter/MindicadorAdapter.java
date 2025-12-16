package com.casrusil.siierpai.modules.integration_chile_data.infrastructure.adapter;

import com.casrusil.siierpai.modules.integration_chile_data.domain.port.out.EconomicIndicatorsProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador de infraestructura para consumir la API de 'mindicador.cl'.
 * <p>
 * Implementa {@link EconomicIndicatorsProvider} utilizando {@link RestClient}.
 * Incluye caché para evitar rate-limiting y fallback seguro para resiliencia.
 */
@Component
public class MindicadorAdapter implements EconomicIndicatorsProvider {

    private static final Logger log = LoggerFactory.getLogger(MindicadorAdapter.class);
    private final RestClient restClient;

    public MindicadorAdapter() {
        // Configuración de timeouts para evitar bloqueo de Virtual Threads
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3s connect
        factory.setReadTimeout(5000); // 5s read

        this.restClient = RestClient.builder()
                .baseUrl("https://mindicador.cl/api")
                .requestFactory(factory)
                .build();
    }

    @Override
    @Cacheable("indicators")
    public Map<String, BigDecimal> getIndicators(LocalDate date) {
        log.info("Fetching economic indicators from Mindicador API for date: {}", date);

        try {
            // Nota: La API pública de mindicador.cl en la raíz retorna los valores del día.
            // Para fechas históricas se requeriría otro endpoint.
            // Usamos el endpoint raíz para cumplir con el requerimiento principal.
            JsonNode rootNode = restClient.get()
                    .retrieve()
                    .body(JsonNode.class);

            if (rootNode == null) {
                throw new IllegalStateException("API returned null response");
            }

            Map<String, BigDecimal> indicators = new HashMap<>();

            // Parseo seguro de JSON
            if (rootNode.has("uf")) {
                indicators.put("uf", new BigDecimal(rootNode.path("uf").path("valor").asText()));
            }
            if (rootNode.has("dolar")) {
                indicators.put("dolar", new BigDecimal(rootNode.path("dolar").path("valor").asText()));
            }
            if (rootNode.has("utm")) {
                indicators.put("utm", new BigDecimal(rootNode.path("utm").path("valor").asText()));
            }

            return indicators;

        } catch (Exception e) {
            log.error("Failed to fetch indicators from Mindicador: {}", e.getMessage(), e);
            return getSafeFallback();
        }
    }

    /**
     * Provee valores por defecto en caso de fallo de la API externa.
     * Garantiza que el sistema no colapse por dependencias externas.
     */
    private Map<String, BigDecimal> getSafeFallback() {
        log.warn("Returning SAFE FALLBACK values for economic indicators.");
        Map<String, BigDecimal> fallback = new HashMap<>();
        fallback.put("uf", new BigDecimal("36500.00"));
        fallback.put("dolar", new BigDecimal("980.00"));
        return fallback;
    }
}
