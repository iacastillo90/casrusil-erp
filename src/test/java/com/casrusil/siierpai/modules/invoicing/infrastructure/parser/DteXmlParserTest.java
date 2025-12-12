package com.casrusil.siierpai.modules.invoicing.infrastructure.parser;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DteXmlParserTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DteXmlParserTest.class);
    private final DteXmlParser parser = new DteXmlParser();

    @Test
    void shouldParseValidDteXml() {
        // Given
        String xml = """
                <DTE version="1.0">
                    <Documento ID="F33T123">
                        <Encabezado>
                            <IdDoc>
                                <TipoDTE>33</TipoDTE>
                                <Folio>123</Folio>
                                <FchEmis>2023-10-27</FchEmis>
                            </IdDoc>
                            <Emisor>
                                <RUTEmisor>76123456-7</RUTEmisor>
                            </Emisor>
                            <Receptor>
                                <RUTRecep>76987654-3</RUTRecep>
                            </Receptor>
                            <Totales>
                                <MntNeto>1000</MntNeto>
                                <IVA>190</IVA>
                                <MntTotal>1190</MntTotal>
                            </Totales>
                        </Encabezado>
                        <Detalle>
                            <NroLinDet>1</NroLinDet>
                            <NmbItem>Item 1</NmbItem>
                            <QtyItem>1</QtyItem>
                            <PrcItem>1000</PrcItem>
                            <MontoItem>1000</MontoItem>
                            <UnmdItem>UN</UnmdItem>
                        </Detalle>
                    </Documento>
                </DTE>
                """;
        CompanyId companyId = new CompanyId(UUID.randomUUID());

        // When
        Invoice invoice = parser.parse(xml, companyId);
        log.info("Parsed Invoice: {}", invoice);
        log.info("Net Amount: {}", invoice.getNetAmount());
        log.info("Tax Amount: {}", invoice.getTaxAmount());
        log.info("Total Amount: {}", invoice.getTotalAmount());

        // Then
        assertNotNull(invoice);
        assertEquals(companyId, invoice.getCompanyId());
        assertEquals(InvoiceType.FACTURA_ELECTRONICA, invoice.getType());
        assertEquals(123L, invoice.getFolio());
        assertEquals("76123456-7", invoice.getIssuerRut());
        assertEquals("76987654-3", invoice.getReceiverRut());
        assertEquals(LocalDate.of(2023, 10, 27), invoice.getDate());
        assertEquals(0, new BigDecimal("1000").compareTo(invoice.getNetAmount()));
        assertEquals(0, new BigDecimal("190").compareTo(invoice.getTaxAmount()));
        assertEquals(0, new BigDecimal("1190").compareTo(invoice.getTotalAmount()));
        assertEquals(1, invoice.getItems().size());
        assertEquals("Item 1", invoice.getItems().get(0).itemName());
    }
}
