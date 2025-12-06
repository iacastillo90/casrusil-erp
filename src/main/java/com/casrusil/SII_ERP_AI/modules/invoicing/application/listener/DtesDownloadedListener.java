package com.casrusil.SII_ERP_AI.modules.invoicing.application.listener;

import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.event.DtesDownloadedEvent;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.RcvData;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Listener de eventos para procesar DTEs descargados.
 * <p>
 * Escucha el evento `DtesDownloadedEvent` y procesa los XMLs descargados para
 * crear facturas.
 * </p>
 */
@Component
public class DtesDownloadedListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DtesDownloadedListener.class);

    private final CreateInvoiceUseCase createInvoiceUseCase;

    public DtesDownloadedListener(CreateInvoiceUseCase createInvoiceUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
    }

    /**
     * Maneja el evento de DTEs descargados.
     *
     * @param event El evento que contiene la lista de XMLs descargados.
     */
    @Async
    @EventListener
    public void handle(DtesDownloadedEvent event) {
        // Run in company context to ensure isolation if needed by downstream services
        CompanyContext.runInCompanyContext(event.companyId(), () -> {
            for (RcvData data : event.rcvDataList()) {
                try {
                    Invoice invoice = mapToInvoice(event, data);
                    createInvoiceUseCase.createInvoice(invoice);
                } catch (Exception e) {
                    // Log error but continue processing other invoices
                    // In a real system, we might want a dead letter queue
                    log.error("Error processing RCV data for folio {}: {}", data.folio(), e.getMessage(), e);
                }
            }
        });
    }

    private Invoice mapToInvoice(DtesDownloadedEvent event, RcvData data) {
        return Invoice.create(
                event.companyId(),
                InvoiceType.fromCode(data.tipoDte()),
                data.folio(),
                data.rutEmisor(),
                data.razonSocialEmisor(),
                data.fechaEmision(),
                data.montoTotal(),
                data.montoNeto(),
                data.montoIva(),
                Collections.emptyList() // RCV summary doesn't have lines
        );
    }
}
