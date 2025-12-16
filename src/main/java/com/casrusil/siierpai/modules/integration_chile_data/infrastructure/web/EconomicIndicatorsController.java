package com.casrusil.siierpai.modules.integration_chile_data.infrastructure.web;

import com.casrusil.siierpai.modules.integration_chile_data.domain.port.out.EconomicIndicatorsProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para exponer indicadores económicos al Frontend.
 * Permite visualizar datos como UF y Dólar en el Dashboard.
 */
@RestController
@RequestMapping("/api/v1/integration/indicators")
public class EconomicIndicatorsController {

    private final EconomicIndicatorsProvider indicatorsProvider;

    public EconomicIndicatorsController(EconomicIndicatorsProvider indicatorsProvider) {
        this.indicatorsProvider = indicatorsProvider;
    }

    /**
     * Endpoint para obtener los indicadores del día.
     * GET /api/v1/integration/indicators/today
     *
     * @return JSON con los valores y la fuente.
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getIndicatorsToday() {
        Map<String, BigDecimal> indicators = indicatorsProvider.getIndicators(LocalDate.now());

        Map<String, Object> response = new HashMap<>();
        response.put("uf", indicators.getOrDefault("uf", BigDecimal.ZERO));
        response.put("usd", indicators.getOrDefault("dolar", BigDecimal.ZERO));
        response.put("utm", indicators.getOrDefault("utm", BigDecimal.ZERO));
        response.put("source", "mindicador.cl");

        return ResponseEntity.ok(response);
    }
}
