package com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.accounting.application.service.SiiAuditorService;
import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounting/audit")
public class SiiAuditController {

    private final SiiAuditorService siiAuditorService;

    public SiiAuditController(SiiAuditorService siiAuditorService) {
        this.siiAuditorService = siiAuditorService;
    }

    @GetMapping("/sii-mirror")
    public ResponseEntity<SiiAuditReportDTO> getSiiMirrorReport(
            @RequestParam int month,
            @RequestParam int year) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(siiAuditorService.compareWithSii(companyId, month, year));
    }

    @PostMapping("/sii-sync")
    public ResponseEntity<Void> forceSiiSync(@RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok().build();
    }
}
