package com.casrusil.siierpai.modules.invoicing.infrastructure.parser;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
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
import java.util.UUID;

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

            String fechaVencStr = getElementValue(idDoc, "FchVenc");
            LocalDate fechaVenc = (fechaVencStr != null && !fechaVencStr.isEmpty())
                    ? LocalDate.parse(fechaVencStr)
                    : fechaEmision;

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
                String dscItem = getElementValue(detalle, "DscItem");
                String fullName = nmbreItem + (dscItem != null ? " " + dscItem : "");

                // Optional fields
                String qtyStr = getElementValue(detalle, "QtyItem");
                BigDecimal quantity = qtyStr != null ? new BigDecimal(qtyStr) : BigDecimal.ONE;
                String prcStr = getElementValue(detalle, "PrcItem");
                BigDecimal price = prcStr != null ? new BigDecimal(prcStr) : BigDecimal.ZERO;
                String montoItemStr = getElementValue(detalle, "MontoItem");
                BigDecimal amount = montoItemStr != null ? new BigDecimal(montoItemStr) : BigDecimal.ZERO;
                String unmdItem = getElementValue(detalle, "UnmdItem");

                lines.add(new InvoiceLine(nroLinDet, fullName.trim(), null, quantity, price, amount, unmdItem));
            }

            // Manually creating Invoice to set DueDate properly as factory might not expose
            // it yet or defaults it.
            // Using the full constructor logic by adapting a factory or creating a new
            // factory method.
            // Since I updated Invoice.create to default dueDate=date, I need to use a
            // cleaner way.
            // I'll call a new private helper or just use the constructor directly if
            // accessible,
            // BUT constructors are public.
            // However, to be clean, I should add a factory method that takes dueDate.
            // Or I can use the existing full factory if I updated it? I updated
            // getters/fields but factories:
            // I updated factories to pass 'date' as 'dueDate'.
            // I will update the call here to use the constructor directly or the full
            // create.
            // But Invoice.create full version doesn't take dueDate in my last edit... wait.
            // My last edit to Invoice.java:
            // StartLine:72, TargetContent did NOT include dueDate in factories.
            // I updated the constructor to take dueDate.
            // I updated factories to PASS 'date' as 'dueDate' to the constructor.
            // So if I want to set a distinct dueDate, I need a factory that accepts it.
            // I'll assume I can use the constructor directly here since I'm in
            // infrastructure.

            return new Invoice(
                    UUID.randomUUID(),
                    companyId,
                    InvoiceType.fromCode(tipoDte),
                    folio,
                    rutEmisor,
                    rutReceptor,
                    null, // businessName
                    fechaEmision,
                    fechaVenc, // dueDate
                    montoNeto,
                    montoIva,
                    montoTotal,
                    BigDecimal.ZERO, // fixedAsset
                    BigDecimal.ZERO, // commonUse
                    Invoice.ORIGIN_SII,
                    com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.SALE, // Defaulting to SALE?
                                                                                               // Parsing logic should
                                                                                               // determine type.
                    // Actually DteXmlParser should know if it's Purchase or Sale based on Company
                    // context?
                    // Usually DTE XML is just the doc. Context determines if it's In or Out.
                    // But DteXmlParser returns an Invoice object.
                    // Existing logic relied on Invoice.create defaulting to TransactionType.SALE.
                    // I will preserve that behavior.
                    com.casrusil.siierpai.modules.invoicing.domain.model.PaymentStatus.PENDING,
                    lines,
                    "CLP");

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
