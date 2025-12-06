package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.web;

import com.casrusil.SII_ERP_AI.modules.accounting.application.service.DuplicateInvoiceDetector;
import com.casrusil.SII_ERP_AI.modules.accounting.application.service.SuspiciousAmountDetector;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AuditAlert;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para auditoría preventiva.
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final DuplicateInvoiceDetector duplicateDetector;
    private final SuspiciousAmountDetector suspiciousAmountDetector;

    public AuditController(DuplicateInvoiceDetector duplicateDetector,
            SuspiciousAmountDetector suspiciousAmountDetector) {
        this.duplicateDetector = duplicateDetector;
        this.suspiciousAmountDetector = suspiciousAmountDetector;
    }

    /**
     * Obtiene todas las alertas de auditoría.
     * 
     * GET /api/v1/audit/alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAllAlerts() {
        List<AuditAlert> allAlerts = new ArrayList<>();

        // Detectar duplicados
        List<AuditAlert> duplicates = duplicateDetector.detectDuplicates(CompanyContext.requireCompanyId());
        allAlerts.addAll(duplicates);

        // Detectar montos sospechosos
        List<AuditAlert> suspicious = suspiciousAmountDetector
                .detectSuspiciousAmounts(CompanyContext.requireCompanyId());
        allAlerts.addAll(suspicious);

        // Agrupar por severidad
        Map<String, Long> bySeverity = new HashMap<>();
        bySeverity.put("CRITICAL",
                allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.CRITICAL).count());
        bySeverity.put("WARNING",
                allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.WARNING).count());
        bySeverity.put("INFO", allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.INFO).count());

        Map<String, Object> response = new HashMap<>();
        response.put("totalAlerts", allAlerts.size());
        response.put("bySeverity", bySeverity);
        response.put("alerts", allAlerts.stream().map(this::toDto).toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene solo alertas de duplicados.
     * 
     * GET /api/v1/audit/duplicates
     */
    @GetMapping("/duplicates")
    public ResponseEntity<List<AuditAlertDto>> getDuplicates() {
        List<AuditAlert> duplicates = duplicateDetector.detectDuplicates(CompanyContext.requireCompanyId());
        return ResponseEntity.ok(duplicates.stream().map(this::toDto).toList());
    }

    /**
     * Obtiene solo alertas de montos sospechosos.
     * 
     * GET /api/v1/audit/suspicious
     */
    @GetMapping("/suspicious")
    public ResponseEntity<List<AuditAlertDto>> getSuspicious() {
        List<AuditAlert> suspicious = suspiciousAmountDetector
                .detectSuspiciousAmounts(CompanyContext.requireCompanyId());
        return ResponseEntity.ok(suspicious.stream().map(this::toDto).toList());
    }

    /**
     * Genera un reporte completo de auditoría.
     * 
     * GET /api/v1/audit/report
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> generateReport() {
        List<AuditAlert> allAlerts = new ArrayList<>();
        allAlerts.addAll(duplicateDetector.detectDuplicates(CompanyContext.requireCompanyId()));
        allAlerts.addAll(suspiciousAmountDetector.detectSuspiciousAmounts(CompanyContext.requireCompanyId()));

        long criticalCount = allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.CRITICAL).count();
        long warningCount = allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.WARNING).count();

        boolean canClosePeriod = criticalCount == 0;

        Map<String, Object> report = new HashMap<>();
        report.put("totalIssues", allAlerts.size());
        report.put("criticalIssues", criticalCount);
        report.put("warnings", warningCount);
        report.put("canClosePeriod", canClosePeriod);
        report.put("recommendation", canClosePeriod
                ? "El período puede cerrarse de forma segura."
                : "Resolver problemas críticos antes de cerrar el período.");
        report.put("alerts", allAlerts.stream().map(this::toDto).toList());

        return ResponseEntity.ok(report);
    }

    private AuditAlertDto toDto(AuditAlert alert) {
        return new AuditAlertDto(
                alert.getId().toString(),
                alert.getType().toString(),
                alert.getSeverity().toString(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getAffectedEntityId(),
                alert.getSuggestedAction());
    }

    public record AuditAlertDto(
            String id,
            String type,
            String severity,
            String title,
            String description,
            String affectedEntityId,
            String suggestedAction) {
    }
}
