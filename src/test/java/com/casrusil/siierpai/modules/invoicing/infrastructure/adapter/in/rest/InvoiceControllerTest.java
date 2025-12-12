package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateInvoiceUseCase createInvoiceUseCase;

    @MockBean
    private SearchInvoicesUseCase searchInvoicesUseCase;

    @MockBean
    private com.casrusil.siierpai.modules.sso.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldCreateInvoice() throws Exception {
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        InvoiceController.CreateInvoiceRequest request = new InvoiceController.CreateInvoiceRequest(
                33,
                123L,
                "76123456-7",
                "Emisor SpA",
                LocalDate.now(),
                new BigDecimal("1190"),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                List.of(new InvoiceController.InvoiceLineRequest("Item 1", BigDecimal.ONE, new BigDecimal("1000"),
                        new BigDecimal("1000"))));

        Invoice invoice = Invoice.create(companyId, InvoiceType.FACTURA_ELECTRONICA, 123L, "76123456-7", "76987654-3",
                LocalDate.now(), new BigDecimal("1000"), new BigDecimal("190"), new BigDecimal("1190"),
                Collections.emptyList());

        when(createInvoiceUseCase.createInvoice(any(Invoice.class))).thenReturn(invoice);

        ScopedValue.where(CompanyContext.COMPANY_ID, companyId).run(() -> {
            try {
                mockMvc.perform(post("/api/v1/invoices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.folio").value(123));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @WithMockUser
    void shouldGetInvoices() throws Exception {
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = Invoice.create(companyId, InvoiceType.FACTURA_ELECTRONICA, 123L, "76123456-7", "76987654-3",
                LocalDate.now(), new BigDecimal("1000"), new BigDecimal("190"), new BigDecimal("1190"),
                Collections.emptyList());

        when(searchInvoicesUseCase.getInvoicesByCompany(companyId)).thenReturn(List.of(invoice));

        ScopedValue.where(CompanyContext.COMPANY_ID, companyId).run(() -> {
            try {
                mockMvc.perform(get("/api/v1/invoices"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].folio").value(123));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
