package com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.accounting.application.service.TaxAuditService;
import com.casrusil.siierpai.modules.accounting.domain.model.TaxAuditReport;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/sii/audit")
public class TaxAuditController {

    private final TaxAuditService taxAuditService;

    public TaxAuditController(TaxAuditService taxAuditService) {
        this.taxAuditService = taxAuditService;
    }

    @GetMapping("/report")
    public ResponseEntity<TaxAuditReport> getTaxComplianceAudit(
            @RequestParam int year,
            @RequestParam int month) {

        CompanyId companyId = CompanyContext.requireCompanyId();

        // Simulación: Pasar lista vacía por ahora (o conectar con servicio de
        // importación si existiera interfaz clara)
        var report = taxAuditService.performAudit(companyId, year, month, Collections.emptyList());

        return ResponseEntity.ok(report);
    }
}
