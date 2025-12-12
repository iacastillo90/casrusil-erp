package com.casrusil.siierpai.modules.fees.domain.util;

import java.math.BigDecimal;
import java.time.Year;

public class RetentionRateCalculator {

    public static BigDecimal getRate(Year year) {
        int yearValue = year.getValue();
        return switch (yearValue) {
            case 2024 -> new BigDecimal("0.1375");
            case 2025 -> new BigDecimal("0.1450");
            case 2026 -> new BigDecimal("0.1525");
            case 2027 -> new BigDecimal("0.1600");
            default -> {
                if (yearValue < 2024)
                    yield new BigDecimal("0.1300"); // Legacy
                yield new BigDecimal("0.1700"); // Future cap
            }
        };
    }
}
