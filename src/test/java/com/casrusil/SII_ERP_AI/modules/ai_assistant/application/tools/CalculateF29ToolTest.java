package com.casrusil.SII_ERP_AI.modules.ai_assistant.application.tools;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.F29Report;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculateF29ToolTest {

    @Mock
    private F29CalculatorService f29CalculatorService;

    private CalculateF29Tool calculateF29Tool;
    private MockedStatic<CompanyContext> companyContextMock;

    @BeforeEach
    void setUp() {
        calculateF29Tool = new CalculateF29Tool(f29CalculatorService,
                new com.fasterxml.jackson.databind.ObjectMapper());
        companyContextMock = mockStatic(CompanyContext.class);
    }

    @AfterEach
    void tearDown() {
        companyContextMock.close();
    }

    @Test
    void shouldCalculateF29Successfully() {
        // Given
        String periodArg = "2025-12";
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        YearMonth period = YearMonth.parse(periodArg);

        F29Report report = new F29Report(
                period,
                new BigDecimal("10000"), // Sales Taxable
                new BigDecimal("0"), // Sales Exempt
                new BigDecimal("5000"), // Purchases Taxable
                new BigDecimal("0"), // Purchases Exempt
                new BigDecimal("1900"), // VAT Debit
                new BigDecimal("950"), // VAT Credit
                new BigDecimal("950"), // VAT Payable
                java.util.Collections.emptyList() // Evidence IDs
        );

        companyContextMock.when(CompanyContext::requireCompanyId).thenReturn(companyId);
        when(f29CalculatorService.calculateF29(eq(companyId), eq(period))).thenReturn(report);

        // When
        String result = calculateF29Tool.execute(periodArg);

        // Then
        assertTrue(result.contains("F29 Calculation for 2025-12"));
        assertTrue(result.contains("Taxable Sales: $10000"));
        assertTrue(result.contains("Net VAT Payable: $950"));
    }

    @Test
    void shouldHandleInvalidDateFormat() {
        // Given
        String invalidArg = "2025/12";

        // When
        String result = calculateF29Tool.execute(invalidArg);

        // Then
        assertTrue(result.contains("Invalid period format"));
    }

    @Test
    void shouldHandleCalculationError() {
        // Given
        String periodArg = "2025-12";
        CompanyId companyId = new CompanyId(UUID.randomUUID());

        companyContextMock.when(CompanyContext::requireCompanyId).thenReturn(companyId);
        when(f29CalculatorService.calculateF29(any(), any()))
                .thenThrow(new RuntimeException("Calculation failed"));

        // When
        String result = calculateF29Tool.execute(periodArg);

        // Then
        assertTrue(result.contains("Error calculating F29"));
    }
}
