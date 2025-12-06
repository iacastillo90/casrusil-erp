package com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.xml;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Constructor de Sobres de Envío DTE (SetDTE).
 * 
 * <p>
 * Empaqueta múltiples DTEs individuales en un único sobre XML (EnvioDte)
 * para su envío al SII.
 * 
 * <p>
 * El sobre incluye:
 * <ul>
 * <li>Carátula con información del envío (Emisor, Receptor, Cantidad de
 * DTEs).</li>
 * <li>Lista de DTEs firmados individualmente.</li>
 * <li>Firma digital del sobre completo.</li>
 * </ul>
 * 
 * @since 1.0
 */
@Component
public class EnvioDteBuilder {

    public String wrap(String signedDteXml, CompanyId companyId, String rutEmisor, String rutEmpresa) {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        sb.append(
                "<EnvioDTE xmlns=\"http://www.sii.cl/SiiDte\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" xsi:schemaLocation=\"http://www.sii.cl/SiiDte EnvioDTE_v10.xsd\">");

        sb.append("<SetDTE ID=\"SetDoc\">");

        sb.append("<Caratula version=\"1.0\">");
        sb.append("<RutEmisor>").append(rutEmisor).append("</RutEmisor>");
        sb.append("<RutEnvia>").append(rutEmpresa).append("</RutEnvia>"); // Usually the same or representative
        sb.append("<RutReceptor>").append("60803000-K").append("</RutReceptor>"); // SII RUT
        sb.append("<FchResol>").append("2014-08-22").append("</FchResol>"); // Resolution Date (Example)
        sb.append("<NroResol>").append("80").append("</NroResol>"); // Resolution Number (Example)
        sb.append("<TmstFirmaEnv>").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .append("</TmstFirmaEnv>");

        // Subtotals
        sb.append("<SubTotDTE>");
        sb.append("<TpoDTE>").append("33").append("</TpoDTE>"); // Example: Factura Electronica
        sb.append("<NroDTE>").append("1").append("</NroDTE>");
        sb.append("</SubTotDTE>");

        sb.append("</Caratula>");

        // Append Signed DTE
        sb.append(signedDteXml);

        sb.append("</SetDTE>");
        sb.append("</EnvioDTE>");

        return sb.toString();
    }
}
