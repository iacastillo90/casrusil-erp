package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.application.service.DuplicateInvoiceDetector;
import com.casrusil.siierpai.modules.accounting.application.service.SuspiciousAmountDetector;
import com.casrusil.siierpai.modules.accounting.domain.model.AuditAlert;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
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
                                allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.CRITICAL)
                                                .count());
                bySeverity.put("WARNING",
                                allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.WARNING).count());
                bySeverity.put("INFO",
                                allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.INFO).count());

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
         * Obtiene el detalle de conciliación tributaria (Compulsa RCV vs ERP).
         * GET /api/v1/audit/reconciliation?period=2025-10
         */
        @GetMapping("/reconciliation")
        // @PreAuthorize("hasAuthority('AUDIT_READ')") // TODO: Descomentar cuando
        // Security esté listo
        public ResponseEntity<List<com.casrusil.siierpai.modules.accounting.infrastructure.web.dto.TaxReconciliationDetailDto>> getTaxReconciliation(
                        @RequestParam(defaultValue = "2025-10") String period) {

                var companyId = CompanyContext.requireCompanyId();

                // NOTA: Aquí deberías llamar a un servicio dedicado (ej:
                // TaxReconciliationService)
                // que cruce tus entidades InvoiceEntity con los datos descargados del RCV.
                // Simulamos la respuesta con datos reales basados en tu estructura:

                List<com.casrusil.siierpai.modules.accounting.infrastructure.web.dto.TaxReconciliationDetailDto> details = new ArrayList<>();

                // Ejemplo 1: Cuadratura perfecta
                details.add(new com.casrusil.siierpai.modules.accounting.infrastructure.web.dto.TaxReconciliationDetailDto(
                                "1", "2025-10", "Factura Electrónica", 18670L, "76.123.456-7",
                                "PROVEEDORES DEL SUR SPA", // ✅ Razón Social
                                new java.math.BigDecimal("18670"), new java.math.BigDecimal("18670"),
                                "MATCH", java.math.BigDecimal.ZERO));

                // Ejemplo 2: Falta en ERP (Tu caso de $98.175.000)
                details.add(new com.casrusil.siierpai.modules.accounting.infrastructure.web.dto.TaxReconciliationDetailDto(
                                "2", "2025-10", "Factura Electrónica", 482L, "76.244.055-5",
                                "SOCIEDAD DE MARKETING Y FIDELIZACION SPA", // ✅ Dato real de tu CSV
                                new java.math.BigDecimal("98175000"), java.math.BigDecimal.ZERO,
                                "MISSING_IN_ERP", new java.math.BigDecimal("98175000")));

                return ResponseEntity.ok(details);
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

                long criticalCount = allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.CRITICAL)
                                .count();
                long warningCount = allAlerts.stream().filter(a -> a.getSeverity() == AuditAlert.Severity.WARNING)
                                .count();

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
