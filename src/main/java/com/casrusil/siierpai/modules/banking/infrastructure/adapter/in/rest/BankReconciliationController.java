package com.casrusil.siierpai.modules.banking.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.banking.application.dto.ReconciliationDashboardDTO;
import com.casrusil.siierpai.modules.banking.application.service.BankReconciliationWorkbenchService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/banking/reconciliation")
public class BankReconciliationController {

    private final BankReconciliationWorkbenchService workbenchService;

    public BankReconciliationController(BankReconciliationWorkbenchService workbenchService) {
        this.workbenchService = workbenchService;
    }

    @GetMapping("/workbench")
    public ResponseEntity<ReconciliationDashboardDTO> getWorkbench() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(workbenchService.getDashboard(companyId));
    }

    @PostMapping("/match")
    public ResponseEntity<Void> matchTransaction(@RequestBody MatchRequest request) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        workbenchService.processMatch(companyId, request.bankId(), request.erpId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadStatement(@RequestParam("file") MultipartFile file) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        try {
            workbenchService.importBankStatement(file.getInputStream(), file.getOriginalFilename(), companyId);
            return ResponseEntity.ok().build();
        } catch (java.io.IOException e) {
            return ResponseEntity.badRequest().body("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    public record MatchRequest(UUID bankId, UUID erpId) {
    }
}
