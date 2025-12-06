package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.persistence.repository;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    void shouldSaveAndFindInvoice() {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = Invoice.create(
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76123456-7",
                "76987654-3",
                LocalDate.now(),
                new BigDecimal("1190"),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                List.of(new InvoiceLine(1, "Item 1", "C1", BigDecimal.ONE, new BigDecimal("1000"),
                        new BigDecimal("1000"), "UN")));

        // When
        invoiceRepository.save(invoice);
        Invoice foundInvoice = invoiceRepository.findById(invoice.getId()).orElse(null);

        // Then
        assertNotNull(foundInvoice);
        assertEquals(invoice.getId(), foundInvoice.getId());
        assertEquals(invoice.getFolio(), foundInvoice.getFolio());
        assertEquals(1, foundInvoice.getItems().size());
        assertEquals("Item 1", foundInvoice.getItems().get(0).itemName());
    }
}
