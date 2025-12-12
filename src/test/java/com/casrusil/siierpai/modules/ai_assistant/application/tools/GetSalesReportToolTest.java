package com.casrusil.siierpai.modules.ai_assistant.application.tools;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSalesReportToolTest {

    @Mock
    private SearchInvoicesUseCase searchInvoicesUseCase;

    @InjectMocks
    private GetSalesReportTool getSalesReportTool;

    @Test
    void shouldGenerateSalesReport() throws Exception {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice1 = Invoice.create(
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76123456-7",
                "76987654-3",
                LocalDate.of(2025, 12, 15),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                Collections.emptyList());

        Invoice invoice2 = Invoice.create(
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                124L,
                "76123456-7",
                "76987654-3",
                LocalDate.of(2025, 12, 20),
                new BigDecimal("2000"),
                new BigDecimal("380"),
                new BigDecimal("2380"),
                Collections.emptyList());

        when(searchInvoicesUseCase.getInvoicesByCompany(any(CompanyId.class)))
                .thenReturn(List.of(invoice1, invoice2));

        // When
        String result = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> getSalesReportTool.execute("2025-12"));

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Sales Report for 2025-12"));
        assertTrue(result.contains("Total Invoices: 2"));
        assertTrue(result.contains("Net Amount: $3000"));
        assertTrue(result.contains("Tax Amount: $570"));
        assertTrue(result.contains("Gross Amount: $3570"));
    }

    @Test
    void shouldHandleCurrentMonth() throws Exception {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        when(searchInvoicesUseCase.getInvoicesByCompany(any(CompanyId.class)))
                .thenReturn(Collections.emptyList());

        // When
        String result = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> getSalesReportTool.execute("current"));

        // Then
        assertNotNull(result);
        assertTrue(result.contains("No sales found"));
    }

    @Test
    void shouldHandleInvalidPeriod() throws Exception {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());

        // When
        String result = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> getSalesReportTool.execute("invalid"));

        // Then
        assertTrue(result.contains("Invalid period format"));
    }

    @Test
    void shouldHaveCorrectMetadata() {
        assertEquals("get_sales_report", getSalesReportTool.name());
        assertNotNull(getSalesReportTool.description());
        assertTrue(getSalesReportTool.description().contains("sales report"));
    }
}
