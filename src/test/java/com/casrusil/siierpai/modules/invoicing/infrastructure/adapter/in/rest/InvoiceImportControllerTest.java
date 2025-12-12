package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.fees.application.service.FeeReceiptService;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvoiceImportControllerTest {

    private MockMvc mockMvc;
    private StubCreateInvoiceUseCase createInvoiceUseCase;
    private CompanyRepository companyRepository;
    private FeeReceiptService feeReceiptService;

    @BeforeEach
    void setUp() {
        createInvoiceUseCase = new StubCreateInvoiceUseCase();
        companyRepository = org.mockito.Mockito.mock(CompanyRepository.class);
        feeReceiptService = org.mockito.Mockito.mock(FeeReceiptService.class);

        // Mock Company retrieval
        org.mockito.Mockito.when(companyRepository.findById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.Optional.of(Company.create("76123456-7", "Test SpA", "test@test.com")));

        InvoiceImportController controller = new InvoiceImportController(createInvoiceUseCase, companyRepository,
                feeReceiptService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void importSiiCsv_shouldParseAndCreateInvoices() throws Exception {
        String csvContent = "Tipo Doc,Folio,RUT Emisor,RUT Receptor,Fecha Emision,Monto Neto,Monto IVA,Monto Total\n" +
                "33,1001,76123456-7,12345678-9,2023-10-27,100000,19000,119000";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoices.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent.getBytes());

        UUID companyId = UUID.randomUUID();

        mockMvc.perform(multipart("/api/v1/invoices/import/sii-csv")
                .file(file)
                .param("companyId", companyId.toString())
                .param("bookType", "SALE"))
                .andExpect(status().isOk());

        assertEquals(1, createInvoiceUseCase.createdInvoices.size());
        assertEquals(Invoice.ORIGIN_MANUAL, createInvoiceUseCase.createdInvoices.get(0).getOrigin());
    }

    static class StubCreateInvoiceUseCase implements CreateInvoiceUseCase {
        List<Invoice> createdInvoices = new ArrayList<>();

        @Override
        public Invoice createInvoice(Invoice invoice) {
            createdInvoices.add(invoice);
            return invoice;
        }
    }
}
