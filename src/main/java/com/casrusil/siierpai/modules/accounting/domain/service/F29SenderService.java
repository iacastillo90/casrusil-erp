package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class F29SenderService {

    private static final Logger log = LoggerFactory.getLogger(F29SenderService.class);

    /**
     * Simulates sending the F29 declaration to the SII.
     * In a real production environment, this would sign the XML and send it via
     * SOAP/REST.
     * 
     * @param companyId Company ID
     * @param report    The calculated F29 report
     * @return A transaction ID (folio) from SII
     */
    public String sendDeclaration(CompanyId companyId, F29Report report) {
        log.info("Preparing F29 Declaration for Company: {} Period: {}", companyId.value(), report.period());

        validateReport(report);

        // Simulate XML generation
        String xmlPayload = generateXmlPayload(report);
        log.debug("Generated XML Payload: {}", xmlPayload);

        // Simulate Network Call
        try {
            Thread.sleep(1000); // Simulate latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate Success Response
        String transactionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("F29 Declaration Submitted Successfully. Transaction ID: {}", transactionId);

        return transactionId;
    }

    private void validateReport(F29Report report) {
        if (report.vatPayable().signum() < 0) {
            log.warn("VAT Payable is negative (Credit Balance). This is valid but requires carry-over logic.");
        }
        // Add more validations here
    }

    private String generateXmlPayload(F29Report report) {
        return String.format("""
                <F29>
                    <Periodo>%s</Periodo>
                    <VentasAfectas>%s</VentasAfectas>
                    <ComprasAfectas>%s</ComprasAfectas>
                    <ImpuestoPagar>%s</ImpuestoPagar>
                </F29>
                """, report.period(), report.totalSalesTaxable(), report.totalPurchasesTaxable(), report.vatPayable());
    }
}
