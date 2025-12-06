package com.casrusil.SII_ERP_AI.modules.ai_assistant.application.tools;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
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
class SearchInvoicesToolTest {

    @Mock
    private SearchInvoicesUseCase searchInvoicesUseCase;

    @InjectMocks
    private SearchInvoicesTool searchInvoicesTool;

    @Test
    void shouldReturnInvoiceList() throws Exception {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = Invoice.create(
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76123456-7",
                "76987654-3",
                LocalDate.now(),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                Collections.emptyList());

        when(searchInvoicesUseCase.getInvoicesByCompany(any(CompanyId.class)))
                .thenReturn(List.of(invoice));

        // When
        String result = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> searchInvoicesTool.execute(""));

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Invoice #123"));
        assertTrue(result.contains("76123456-7"));
        assertTrue(result.contains("1190"));
    }

    @Test
    void shouldReturnNoInvoicesMessage() throws Exception {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        when(searchInvoicesUseCase.getInvoicesByCompany(any(CompanyId.class)))
                .thenReturn(Collections.emptyList());

        // When
        String result = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> searchInvoicesTool.execute(""));

        // Then
        assertEquals("No invoices found.", result);
    }

    @Test
    void shouldHaveCorrectMetadata() {
        assertEquals("search_invoices", searchInvoicesTool.name());
        assertNotNull(searchInvoicesTool.description());
        assertTrue(searchInvoicesTool.description().contains("invoices"));
    }
}
