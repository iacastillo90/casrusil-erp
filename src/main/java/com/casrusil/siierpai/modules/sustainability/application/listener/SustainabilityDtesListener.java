package com.casrusil.siierpai.modules.sustainability.application.listener;

import com.casrusil.siierpai.modules.integration_sii.domain.event.DtesDownloadedEvent;
import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceEntity;
import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.repository.InvoiceJpaRepository;
import com.casrusil.siierpai.modules.sustainability.application.service.CarbonFootprintCalculator;
import com.casrusil.siierpai.modules.sustainability.domain.model.CarbonFootprintResult;
import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.entity.SustainabilityRecordEntity;
import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.repository.SustainabilityRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class SustainabilityDtesListener {

    private static final Logger log = LoggerFactory.getLogger(SustainabilityDtesListener.class);

    private final InvoiceJpaRepository invoiceRepository;
    private final CarbonFootprintCalculator calculator;
    private final SustainabilityRecordRepository sustainabilityRepository;

    public SustainabilityDtesListener(InvoiceJpaRepository invoiceRepository,
            CarbonFootprintCalculator calculator,
            SustainabilityRecordRepository sustainabilityRepository) {
        this.invoiceRepository = invoiceRepository;
        this.calculator = calculator;
        this.sustainabilityRepository = sustainabilityRepository;
    }

    @Async
    @EventListener
    public void onDtesDownloaded(DtesDownloadedEvent event) {
        log.info("Procesando evento de descarga de DTEs para sostenibilidad. Company: {}",
                event.companyId().getValue());

        for (RcvData dte : event.rcvDataList()) {
            try {
                processDte(event.companyId().getValue(), dte);
            } catch (Exception e) {
                log.error("Error calculando huella para DTE folio {}: {}", dte.folio(), e.getMessage());
            }
        }
    }

    private void processDte(UUID companyId, RcvData dte) {
        // Intentar buscar la factura persistida
        // Nota: Es posible que la factura aún no se haya persistido si el evento es muy
        // rápido.
        // En un escenario real, quizás deberíamos escuchar un evento
        // InvoiceCreatedEvent.
        // Pero intentamos aquí según requerimiento.

        Optional<InvoiceEntity> invoiceOpt = invoiceRepository.findByCompanyIdAndTypeCodeAndFolioAndIssuerRut(
                companyId, dte.tipoDte(), dte.folio(), dte.rutEmisor());

        if (invoiceOpt.isPresent()) {
            InvoiceEntity invoice = invoiceOpt.get();

            // Verificar si ya existe el cálculo
            // (Asumimos que Invoice ID es la clave para verificar si ya procesamos, pero
            // no tenemos un método existsByInvoiceId en SustainabilityRepo aún, usaremos
            // try-catch o verificación manual si fuera necesario)
            // Dado que la relación es OneToOne, si intentamos guardar otro, fallará por
            // unique constraint.
            // Omitiremos comprobación explicita por ahora o capturamos excepción.

            CarbonFootprintResult result = calculator.calculate(invoice);

            if (result.totalCarbonFootprint().doubleValue() > 0) {
                SustainabilityRecordEntity record = new SustainabilityRecordEntity(
                        UUID.randomUUID(),
                        invoice,
                        result.totalCarbonFootprint(),
                        result.category(),
                        result.confidenceScore());
                sustainabilityRepository.save(record);
                log.debug("Huella calculada para factura {}: {} kgCO2e", invoice.getFolio(),
                        result.totalCarbonFootprint());
            }
        } else {
            log.debug("Factura no encontrada para cálculo de huella: Folio {}", dte.folio());
        }
    }
}
