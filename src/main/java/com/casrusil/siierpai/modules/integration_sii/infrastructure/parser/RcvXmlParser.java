package com.casrusil.siierpai.modules.integration_sii.infrastructure.parser;

import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para archivos XML del Registro de Compras y Ventas (RCV).
 * 
 * <p>
 * Transforma la respuesta XML del SII (que contiene listas de facturas)
 * en objetos de dominio {@link SiiDteSummary}.
 * 
 * <p>
 * Maneja la estructura específica del XML del SII, incluyendo namespaces
 * y formatos de fecha.
 * 
 * @see SiiDteSummary
 * @since 1.0
 */
@Component
public class RcvXmlParser {

    public List<RcvData> parse(String xml) {
        List<RcvData> rcvDataList = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));

            // Assuming a standard SII response structure, e.g., <Detalle> items
            NodeList detalles = doc.getElementsByTagName("Detalle");

            for (int i = 0; i < detalles.getLength(); i++) {
                Node node = detalles.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    rcvDataList.add(mapToRcvData(element));
                }
            }
        } catch (Exception e) {
            throw new com.casrusil.siierpai.modules.integration_sii.domain.exception.SiiParsingException(
                    "Error parsing RCV XML", e);
        }
        return rcvDataList;
    }

    private RcvData mapToRcvData(Element element) {
        return new RcvData(
                getInteger(element, "TipoDte"),
                getLong(element, "Folio"),
                getString(element, "RutEmisor"),
                getString(element, "RazonSocial"),
                getDate(element, "FechaEmision"),
                getBigDecimal(element, "MontoTotal"),
                getBigDecimal(element, "MontoNeto"),
                getBigDecimal(element, "MontoIva"),
                getString(element, "Estado"));
    }

    private String getString(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private Integer getInteger(Element element, String tagName) {
        String val = getString(element, tagName);
        return val != null ? Integer.parseInt(val) : null;
    }

    private Long getLong(Element element, String tagName) {
        String val = getString(element, tagName);
        return val != null ? Long.parseLong(val) : null;
    }

    private BigDecimal getBigDecimal(Element element, String tagName) {
        String val = getString(element, tagName);
        return val != null ? new BigDecimal(val) : BigDecimal.ZERO;
    }

    private LocalDate getDate(Element element, String tagName) {
        String val = getString(element, tagName);
        // SII dates are usually YYYY-MM-DD
        return val != null ? LocalDate.parse(val, DateTimeFormatter.ISO_DATE) : null;
    }
}
