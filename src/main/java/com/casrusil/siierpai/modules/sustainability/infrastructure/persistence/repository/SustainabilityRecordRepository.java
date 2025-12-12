package com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.entity.SustainabilityRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SustainabilityRecordRepository extends JpaRepository<SustainabilityRecordEntity, UUID> {

    @Query("SELECT SUM(s.carbonFootprintKg) FROM SustainabilityRecordEntity s WHERE s.calculatedAt BETWEEN :startDate AND :endDate")
    BigDecimal sumCarbonFootprintBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s.categoryName, SUM(s.carbonFootprintKg) FROM SustainabilityRecordEntity s WHERE s.calculatedAt BETWEEN :startDate AND :endDate GROUP BY s.categoryName ORDER BY SUM(s.carbonFootprintKg) DESC")
    List<Object[]> findTopEmittingCategoriesBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
