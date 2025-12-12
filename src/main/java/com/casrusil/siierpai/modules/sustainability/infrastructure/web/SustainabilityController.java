package com.casrusil.siierpai.modules.sustainability.infrastructure.web;

import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.repository.SustainabilityRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sustainability")
public class SustainabilityController {

    private final SustainabilityRecordRepository repository;

    public SustainabilityController(SustainabilityRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = YearMonth.from(now).atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.from(now).atEndOfMonth().atTime(23, 59, 59);

        // 1. Total Carbon Footprint (Current Month)
        BigDecimal totalCarbon = repository.sumCarbonFootprintBetween(startOfMonth, endOfMonth);
        if (totalCarbon == null)
            totalCarbon = BigDecimal.ZERO;

        // 2. Top Emitting Categories (Last 3 months to be more representative)
        List<Object[]> topCategoriesData = repository.findTopEmittingCategoriesBetween(now.minusMonths(3), now);
        List<String> topCategories = new ArrayList<>();
        for (int i = 0; i < Math.min(3, topCategoriesData.size()); i++) {
            topCategories.add((String) topCategoriesData.get(i)[0]);
        }

        // 3. Monthly Trend (Last 6 months)
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now.minusMonths(i));
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);
            BigDecimal val = repository.sumCarbonFootprintBetween(start, end);

            Map<String, Object> point = new HashMap<>();
            point.put("month", ym.toString());
            point.put("value", val != null ? val : BigDecimal.ZERO);
            trend.add(point);
        }

        // 4. Green Score (Mock logic for now: 100 - (footprint / 100))
        // En producción esto sería más complejo (comparando con industria).
        int score = Math.max(0, 100 - (totalCarbon.intValue() / 100));

        Map<String, Object> response = new HashMap<>();
        response.put("totalCarbonFootprint", totalCarbon);
        response.put("monthlyTrend", trend);
        response.put("topEmittingCategories", topCategories);
        response.put("greenScore", score);

        return ResponseEntity.ok(response);
    }
}
