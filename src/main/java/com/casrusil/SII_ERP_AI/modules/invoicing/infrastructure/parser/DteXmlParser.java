package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.parser;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DteXmlParser {

    public Invoice parse(String xmlContent, CompanyId companyId) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlContent)));

            // Basic parsing logic for standard DTE
            // Structure: DTE -> Documento -> Encabezado -> IdDoc, Emisor, Receptor, Totales
            Element encabezado = (Element) doc.getElementsByTagName("Encabezado").item(0);
            Element idDoc = (Element) encabezado.getElementsByTagName("IdDoc").item(0);
            Element emisor = (Element) encabezado.getElementsByTagName("Emisor").item(0);
            Element receptor = (Element) encabezado.getElementsByTagName("Receptor").item(0);
            Element totales = (Element) encabezado.getElementsByTagName("Totales").item(0);

            int tipoDte = Integer.parseInt(getElementValue(idDoc, "TipoDTE"));
            long folio = Long.parseLong(getElementValue(idDoc, "Folio"));
            String fechaEmisionStr = getElementValue(idDoc, "FchEmis");
            LocalDate fechaEmision = LocalDate.parse(fechaEmisionStr);

            String rutEmisor = getElementValue(emisor, "RUTEmisor");
            String rutReceptor = getElementValue(receptor, "RUTRecep");

            BigDecimal montoNeto = new BigDecimal(getElementValue(totales, "MntNeto"));
            // IVA might be optional (e.g. Exenta)
            String ivaStr = getElementValue(totales, "IVA");
            BigDecimal montoIva = ivaStr != null ? new BigDecimal(ivaStr) : BigDecimal.ZERO;
            BigDecimal montoTotal = new BigDecimal(getElementValue(totales, "MntTotal"));

            List<InvoiceLine> lines = new ArrayList<>();
            NodeList detalles = doc.getElementsByTagName("Detalle");
            for (int i = 0; i < detalles.getLength(); i++) {
                Element detalle = (Element) detalles.item(i);
                int nroLinDet = Integer.parseInt(getElementValue(detalle, "NroLinDet"));
                String nmbreItem = getElementValue(detalle, "NmbItem");
                // Optional fields
                String qtyStr = getElementValue(detalle, "QtyItem");
                BigDecimal quantity = qtyStr != null ? new BigDecimal(qtyStr) : BigDecimal.ONE;
                String prcStr = getElementValue(detalle, "PrcItem");
                BigDecimal price = prcStr != null ? new BigDecimal(prcStr) : BigDecimal.ZERO;
                String montoItemStr = getElementValue(detalle, "MontoItem");
                BigDecimal amount = montoItemStr != null ? new BigDecimal(montoItemStr) : BigDecimal.ZERO;
                String unmdItem = getElementValue(detalle, "UnmdItem");

                lines.add(new InvoiceLine(nroLinDet, nmbreItem, null, quantity, price, amount, unmdItem));
            }

            return Invoice.create(
                    companyId,
                    InvoiceType.fromCode(tipoDte),
                    folio,
                    rutEmisor,
                    rutReceptor,
                    fechaEmision,
                    montoNeto,
                    montoIva,
                    montoTotal,
                    lines);

        } catch (Exception e) {
            throw new RuntimeException("Error parsing DTE XML", e);
        }
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            return list.item(0).getTextContent().trim();
        }
        return null;
    }
}
