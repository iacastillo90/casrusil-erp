package com.casrusil.siierpai.modules.treasury.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.accounting.application.service.CashFlowProjectionService;
import com.casrusil.siierpai.modules.accounting.application.service.FinancialRatiosService;
import com.casrusil.siierpai.modules.treasury.application.service.CashManagementService;
import com.casrusil.siierpai.modules.treasury.domain.model.CashTransaction;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/treasury")
public class TreasuryController {

    private final CashManagementService cashManagementService;
    private final CashFlowProjectionService cashFlowProjectionService;
    private final FinancialRatiosService financialRatiosService; // ✅ Inyectamos el nuevo servicio

    public TreasuryController(CashManagementService cashManagementService,
            CashFlowProjectionService cashFlowProjectionService,
            FinancialRatiosService financialRatiosService) {
        this.cashManagementService = cashManagementService;
        this.cashFlowProjectionService = cashFlowProjectionService;
        this.financialRatiosService = financialRatiosService;
    }

    // ✅ Endpoint existente
    @GetMapping("/cash-flow-daily")
    public ResponseEntity<CashFlowProjectionService.DailyCashFlowReport> getDailyCashFlow(
            @RequestParam int year,
            @RequestParam int month) {

        CompanyId companyId = CompanyContext.requireCompanyId();
        var report = cashFlowProjectionService.getDailyCashFlow(companyId, year, month);

        return ResponseEntity.ok(report);
    }

    // ✅ NUEVO ENDPOINT: Ratios Financieros
    @GetMapping("/financial-ratios")
    public ResponseEntity<FinancialRatiosService.LiquidityReport> getFinancialRatios(
            @RequestParam int year,
            @RequestParam int month) {

        CompanyId companyId = CompanyContext.requireCompanyId();
        var report = financialRatiosService.getLiquidityRatios(companyId, year, month);

        return ResponseEntity.ok(report);
    }

    @PostMapping("/cash-entry")
    public ResponseEntity<Void> recordCashEntry(@RequestBody CashEntryRequest request) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        CashTransaction transaction = CashTransaction.create(
                companyId, request.date(), request.description(),
                request.amount(), request.reference(), request.category());
        cashManagementService.recordCashTransaction(transaction);
        return ResponseEntity.ok().build();
    }

    public record CashEntryRequest(LocalDate date, String description, BigDecimal amount, String reference,
            String category) {
    }
}
