package com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.adapter.in.rest;

import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.RcvData;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.port.in.AuthenticateSiiUseCase;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.port.in.DownloadRcvUseCase;
import com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones directas con el SII.
 * 
 * <p>
 * Expone funcionalidades de integración como la descarga manual del RCV
 * (Registro de Compras y Ventas). Útil para pruebas y sincronización bajo
 * demanda.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/sii/ops/fetch-rcv}: Descargar RCV desde el SII.</li>
 * </ul>
 * 
 * @see DownloadRcvUseCase
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/sii/ops")
public class SiiIntegrationController {

    private final AuthenticateSiiUseCase authenticateSiiUseCase;
    private final DownloadRcvUseCase downloadRcvUseCase;
    private final Pkcs12Handler pkcs12Handler;
    private final com.casrusil.SII_ERP_AI.shared.domain.event.EventPublisher eventPublisher;

    @Value("${sii.test.cert.path:}")
    private String certPath;

    @Value("${sii.test.cert.password:}")
    private String certPassword;

    public SiiIntegrationController(AuthenticateSiiUseCase authenticateSiiUseCase,
            DownloadRcvUseCase downloadRcvUseCase,
            Pkcs12Handler pkcs12Handler,
            com.casrusil.SII_ERP_AI.shared.domain.event.EventPublisher eventPublisher) {
        this.authenticateSiiUseCase = authenticateSiiUseCase;
        this.downloadRcvUseCase = downloadRcvUseCase;
        this.pkcs12Handler = pkcs12Handler;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/fetch-rcv")
    public ResponseEntity<List<RcvData>> fetchRcv(@RequestParam String rutEmpresa,
            @RequestParam String period,
            @RequestParam(defaultValue = "true") boolean isPurchase) {

        if (certPath.isEmpty() || certPassword.isEmpty()) {
            throw new IllegalStateException(
                    "SII Certificate not configured. Set sii.test.cert.path and sii.test.cert.password");
        }

        CompanyId companyId;
        try {
            companyId = CompanyContext.requireCompanyId();
        } catch (IllegalStateException e) {
            // Fallback for testing without auth context
            companyId = new CompanyId(java.util.UUID.randomUUID());
        }

        // 1. Load Certificate
        SiiCertificate certificate = pkcs12Handler.loadCertificate(certPath, certPassword);

        // 2. Authenticate
        SiiToken token = authenticateSiiUseCase.authenticate(certificate);

        // 3. Download RCV
        List<RcvData> rcvData;
        if (isPurchase) {
            rcvData = downloadRcvUseCase.downloadPurchaseRegister(token, companyId, rutEmpresa, period);
        } else {
            rcvData = downloadRcvUseCase.downloadSalesRegister(token, companyId, rutEmpresa, period);
        }

        // 4. Publish Event
        eventPublisher.publish(new com.casrusil.SII_ERP_AI.modules.integration_sii.domain.event.RcvDownloadedEvent(
                companyId, rcvData));

        return ResponseEntity.ok(rcvData);
    }
}
