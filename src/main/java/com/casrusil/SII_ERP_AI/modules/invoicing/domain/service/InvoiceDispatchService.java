package com.casrusil.SII_ERP_AI.modules.invoicing.domain.service;

import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.Caf;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.barcode.Pdf417Generator;
import com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.crypto.TedGenerator;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.mail.InvoiceEmailSender;
import com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.pdf.InvoicePdfGenerator;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InvoiceDispatchService {

    private final InvoiceRepository invoiceRepository;
    private final CafRepository cafRepository;
    private final TedGenerator tedGenerator;
    private final Pdf417Generator pdf417Generator;
    private final InvoicePdfGenerator invoicePdfGenerator;
    private final InvoiceEmailSender invoiceEmailSender;

    public InvoiceDispatchService(
            InvoiceRepository invoiceRepository,
            CafRepository cafRepository,
            TedGenerator tedGenerator,
            Pdf417Generator pdf417Generator,
            InvoicePdfGenerator invoicePdfGenerator,
            InvoiceEmailSender invoiceEmailSender) {
        this.invoiceRepository = invoiceRepository;
        this.cafRepository = cafRepository;
        this.tedGenerator = tedGenerator;
        this.pdf417Generator = pdf417Generator;
        this.invoicePdfGenerator = invoicePdfGenerator;
        this.invoiceEmailSender = invoiceEmailSender;
    }

    public void dispatchInvoice(UUID invoiceId, String recipientEmail) {
        // 1. Fetch Invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        // 2. Get CAF
        String tipoDteStr = String.valueOf(invoice.getType().getCode());
        Caf caf = cafRepository.findActiveForFolio(invoice.getCompanyId(), tipoDteStr, invoice.getFolio())
                .orElseThrow(() -> new IllegalStateException("No active CAF found for invoice " + invoice.getFolio()));

        // 3. Generate TED XML
        String tedXml = tedGenerator.generateTedXml(invoice, caf);

        // 4. Generate TED Image (PDF417)
        byte[] tedImage = pdf417Generator.generatePdf417(tedXml);

        // 5. Generate PDF
        byte[] pdfContent = invoicePdfGenerator.generatePdf(invoice, tedImage);

        // 6. Send Email
        String subject = "Documento Tributario Electrónico N° " + invoice.getFolio();
        String body = "Estimado cliente,\n\nAdjunto encontrará su Factura Electrónica N° " + invoice.getFolio()
                + ".\n\nAtentamente,\n" + invoice.getIssuerRut();
        String filename = "DTE_" + invoice.getFolio() + ".pdf";

        invoiceEmailSender.sendInvoiceEmail(recipientEmail, subject, body, pdfContent, filename);
    }
}
