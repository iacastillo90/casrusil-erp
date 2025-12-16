package com.casrusil.siierpai.shared.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateProvider templateProvider;

    public EmailService(JavaMailSender mailSender, EmailTemplateProvider templateProvider) {
        this.mailSender = mailSender;
        this.templateProvider = templateProvider;
    }

    @Async
    public void sendInvitationEmail(String to, String inviteLink,
            com.casrusil.siierpai.modules.sso.domain.model.Company company, String inviterName) {
        try {
            String subject = "Invitación a unirte a " + company.getRazonSocial();
            String htmlContent = templateProvider.getInvitationTemplate(inviteLink, company.getRazonSocial(),
                    company.getRut(), company.getLogoUrl(), inviterName);
            sendHtmlEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            logger.error("Failed to send invitation email to {}", to, e);
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String userName, String companyName) {
        try {
            String subject = "Bienvenido a SII ERP AI";
            String htmlContent = templateProvider.getWelcomeTemplate(userName, companyName);
            sendHtmlEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            logger.error("Failed to send welcome email to {}", to, e);
        }
    }

    @Async
    public void sendWeeklyReportEmail(String to, byte[] pdfAttachment, String period) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Reporte Semanal: " + period);
            helper.setText(templateProvider.getWeeklyReportTemplate(period), true);

            helper.addAttachment("Reporte_" + period.replace(" ", "_") + ".pdf", new ByteArrayResource(pdfAttachment));

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send weekly report to {}", to, e);
        }
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            sendHtmlEmail(to, subject, body);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}", to, e);
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addAttachment(filename, new ByteArrayResource(attachment));
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send email with attachment to {}", to, e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}
