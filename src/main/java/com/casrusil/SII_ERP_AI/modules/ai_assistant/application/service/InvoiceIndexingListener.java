package com.casrusil.SII_ERP_AI.modules.ai_assistant.application.service;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.out.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for invoice events to automatically index them for RAG.
 */
@Component
public class InvoiceIndexingListener {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceIndexingListener.class);

    private final DocumentIndexingService documentIndexingService;

    public InvoiceIndexingListener(DocumentIndexingService documentIndexingService) {
        this.documentIndexingService = documentIndexingService;
    }

    /**
     * Automatically index invoices when they are created.
     */
    @EventListener
    public void onInvoiceCreated(InvoiceCreatedEvent event) {
        logger.info("Indexing invoice for RAG: {}", event.invoice().getFolio());

        try {
            // Index for RAG using the invoice directly from the event
            documentIndexingService.indexInvoice(event.invoice());

            logger.info("Successfully indexed invoice #{} for RAG", event.invoice().getFolio());

        } catch (Exception e) {
            logger.error("Failed to index invoice {}: {}",
                    event.invoice().getFolio(), e.getMessage(), e);
        }
    }
}
