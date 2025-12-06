package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.pdf;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceLine;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class InvoicePdfGenerator {

    private static final Font FONT_BOLD = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 14, Font.BOLD, Color.RED);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

    public byte[] generatePdf(Invoice invoice, byte[] tedImage) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // 1. Header (Logo + Company Info | Red Box)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[] { 2, 1 });

            // Left: Company Info (Placeholder)
            PdfPCell companyCell = new PdfPCell();
            companyCell.setBorder(Rectangle.NO_BORDER);
            companyCell.addElement(new Paragraph("EMPRESA DEMO S.A.", FONT_BOLD));
            companyCell.addElement(new Paragraph("Giro: Desarrollo de Software", FONT_NORMAL));
            companyCell.addElement(new Paragraph("Dirección: Av. Siempre Viva 123", FONT_NORMAL));
            companyCell.addElement(new Paragraph("Santiago, Chile", FONT_NORMAL));
            headerTable.addCell(companyCell);

            // Right: Red Box (RUT + Folio)
            PdfPCell boxCell = new PdfPCell();
            boxCell.setBorder(Rectangle.BOX);
            boxCell.setBorderColor(Color.RED);
            boxCell.setBorderWidth(2f);
            boxCell.setPadding(10);
            boxCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            boxCell.addElement(createCenteredParagraph("R.U.T.: " + invoice.getIssuerRut(), FONT_TITLE));
            boxCell.addElement(createCenteredParagraph("FACTURA ELECTRONICA", FONT_TITLE));
            boxCell.addElement(createCenteredParagraph("N° " + invoice.getFolio(), FONT_TITLE));

            headerTable.addCell(boxCell);
            document.add(headerTable);

            document.add(new Paragraph("\n")); // Spacer

            // 2. Client Info
            PdfPTable clientTable = new PdfPTable(1);
            clientTable.setWidthPercentage(100);
            PdfPCell clientCell = new PdfPCell();
            clientCell.addElement(new Paragraph("Señor(es): " + "CLIENTE DEMO", FONT_BOLD)); // TODO: Get real name
            clientCell.addElement(new Paragraph("R.U.T.: " + invoice.getReceiverRut(), FONT_BOLD));
            clientCell.addElement(new Paragraph(
                    "Fecha Emisión: " + invoice.getDate().format(DateTimeFormatter.ISO_DATE), FONT_NORMAL));
            clientTable.addCell(clientCell);
            document.add(clientTable);

            document.add(new Paragraph("\n"));

            // 3. Details Table
            PdfPTable detailsTable = new PdfPTable(5); // Item, Qty, Price, Total
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[] { 4, 1, 1, 1, 1 });

            // Headers
            addHeaderCell(detailsTable, "Descripción");
            addHeaderCell(detailsTable, "Cant.");
            addHeaderCell(detailsTable, "Unid.");
            addHeaderCell(detailsTable, "Precio");
            addHeaderCell(detailsTable, "Total");

            // Rows
            if (invoice.getItems() != null) {
                for (InvoiceLine item : invoice.getItems()) {
                    detailsTable.addCell(new Phrase(item.itemName(), FONT_NORMAL));
                    detailsTable.addCell(new Phrase(item.quantity().toString(), FONT_NORMAL));
                    detailsTable.addCell(new Phrase(item.unit(), FONT_NORMAL));
                    detailsTable.addCell(new Phrase(CURRENCY_FORMAT.format(item.price()), FONT_NORMAL));
                    detailsTable.addCell(new Phrase(CURRENCY_FORMAT.format(item.amount()), FONT_NORMAL));
                }
            }
            document.add(detailsTable);

            document.add(new Paragraph("\n"));

            // 4. Totals & TED
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[] { 1, 1 });

            // Left: TED Image
            PdfPCell tedCell = new PdfPCell();
            tedCell.setBorder(Rectangle.NO_BORDER);
            if (tedImage != null && tedImage.length > 0) {
                Image img = Image.getInstance(tedImage);
                img.scalePercent(50); // Scale down
                tedCell.addElement(img);
                tedCell.addElement(new Paragraph("Timbre Electrónico SII", new Font(Font.HELVETICA, 8)));
                tedCell.addElement(new Paragraph("Res. 80 de 2014 - Verifique documento: www.sii.cl",
                        new Font(Font.HELVETICA, 8)));
            }
            footerTable.addCell(tedCell);

            // Right: Totals
            PdfPCell totalsCell = new PdfPCell();
            totalsCell.setBorder(Rectangle.BOX);
            totalsCell.addElement(new Paragraph("Neto: " + CURRENCY_FORMAT.format(invoice.getNetAmount()), FONT_BOLD));
            totalsCell.addElement(
                    new Paragraph("IVA (19%): " + CURRENCY_FORMAT.format(invoice.getTaxAmount()), FONT_BOLD));
            totalsCell
                    .addElement(new Paragraph("Total: " + CURRENCY_FORMAT.format(invoice.getTotalAmount()), FONT_BOLD));
            footerTable.addCell(totalsCell);

            document.add(footerTable);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private Paragraph createCenteredParagraph(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
