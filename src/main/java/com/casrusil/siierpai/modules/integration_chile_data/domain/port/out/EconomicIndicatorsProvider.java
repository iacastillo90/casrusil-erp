package com.casrusil.siierpai.modules.integration_chile_data.domain.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Puerto de salida para proveer indicadores económicos.
 * <p>
 * Define el contrato que debe cumplir cualquier adaptador que obtenga
 * indicadores
 * del mercado financiero chileno (UF, Dólar, UTM, etc.).
 */
public interface EconomicIndicatorsProvider {

    /**
     * Obtiene los indicadores económicos para una fecha específica.
     *
     * @param date Fecha de consulta.
     * @return Mapa con claves (ej: "uf", "dolar") y valores en BigDecimal.
     */
    Map<String, BigDecimal> getIndicators(LocalDate date);

    /**
     * Obtiene el valor de la UF para el día actual.
     * Delegates to {@link #getIndicators(LocalDate)}.
     *
     * @return Valor de la UF.
     */
    default BigDecimal getUF() {
        return getIndicators(LocalDate.now()).get("uf");
    }

    /**
     * Obtiene el valor del Dólar (USD) para el día actual.
     * Delegates to {@link #getIndicators(LocalDate)}.
     *
     * @return Valor del Dólar.
     */
    default BigDecimal getUSD() {
        return getIndicators(LocalDate.now()).get("dolar");
    }
}
