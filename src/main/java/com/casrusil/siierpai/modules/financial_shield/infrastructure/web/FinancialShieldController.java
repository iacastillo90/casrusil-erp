package com.casrusil.siierpai.modules.financial_shield.infrastructure.web;

import com.casrusil.siierpai.modules.financial_shield.application.service.FinancialShieldService;
import com.casrusil.siierpai.modules.financial_shield.domain.model.CashFlowHealth;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financial-shield")
public class FinancialShieldController {

    private final FinancialShieldService service;

    public FinancialShieldController(FinancialShieldService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public ResponseEntity<CashFlowHealth> getHealth() {
        return ResponseEntity.ok(service.analyzeHealth(CompanyContext.requireCompanyId()));
    }
}
