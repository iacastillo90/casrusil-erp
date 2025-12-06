package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.mail;

import com.casrusil.SII_ERP_AI.shared.infrastructure.mail.EmailService;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEmailSender {

    private final EmailService emailService;

    public InvoiceEmailSender(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendInvoiceEmail(String to, String subject, String body, byte[] pdfContent, String pdfFilename) {
        emailService.sendEmailWithAttachment(to, subject, body, pdfContent, pdfFilename);
    }
}
