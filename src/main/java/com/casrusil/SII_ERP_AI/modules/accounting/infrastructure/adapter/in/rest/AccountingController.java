package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.adapter.in.rest;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.Account;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountType;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.ClosedPeriod;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.F29Report;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.service.PeriodClosingService;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.UserId;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * Controlador REST para la gestión contable.
 * 
 * <p>
 * Expone endpoints para consultar asientos, gestionar el plan de cuentas,
 * calcular impuestos mensuales (F29) y realizar el cierre de periodos.
 * 
 * <h2>Endpoints principales:</h2>
 * <ul>
 * <li>{@code GET /api/v1/accounting/entries}: Listar asientos contables.</li>
 * <li>{@code POST /api/v1/accounting/accounts}: Crear cuenta contable.</li>
 * <li>{@code GET /api/v1/accounting/f29}: Calcular borrador F29.</li>
 * <li>{@code POST /api/v1/accounting/periods/close}: Cerrar periodo
 * contable.</li>
 * </ul>
 * 
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/accounting")
public class AccountingController {

    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountRepository accountRepository;
    private final F29CalculatorService f29CalculatorService;
    private final PeriodClosingService periodClosingService;
    private final com.casrusil.SII_ERP_AI.modules.accounting.application.service.OpeningBalanceService openingBalanceService;

    public AccountingController(AccountingEntryRepository accountingEntryRepository,
            AccountRepository accountRepository,
            F29CalculatorService f29CalculatorService,
            PeriodClosingService periodClosingService,
            com.casrusil.SII_ERP_AI.modules.accounting.application.service.OpeningBalanceService openingBalanceService) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountRepository = accountRepository;
        this.f29CalculatorService = f29CalculatorService;
        this.periodClosingService = periodClosingService;
        this.openingBalanceService = openingBalanceService;
    }

    @PostMapping("/opening-balance")
    public ResponseEntity<Void> setOpeningBalance(
            @RequestBody List<com.casrusil.SII_ERP_AI.modules.accounting.application.service.OpeningBalanceService.OpeningBalanceItem> items) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        openingBalanceService.setOpeningBalance(companyId, items);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/entries")
    public ResponseEntity<List<AccountingEntry>> getEntries() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(accountingEntryRepository.findByCompanyId(companyId));
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        Account account = new Account(
                companyId,
                request.code(),
                request.name(),
                request.type(),
                request.description());
        Account savedAccount = accountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAccounts() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(accountRepository.findAll(companyId));
    }

    // ========== F29 Endpoints ==========

    @GetMapping("/f29")
    public ResponseEntity<F29Report> calculateF29(@RequestParam String period) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth yearMonth = YearMonth.parse(period);
        F29Report report = f29CalculatorService.calculateF29(companyId, yearMonth);
        return ResponseEntity.ok(report);
    }

    // ========== Period Closing Endpoints ==========

    @PostMapping("/periods/close")
    public ResponseEntity<ClosedPeriod> closePeriod(
            @RequestParam String period,
            @RequestParam String userId) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth yearMonth = YearMonth.parse(period);
        UserId user = new UserId(java.util.UUID.fromString(userId));
        ClosedPeriod closedPeriod = periodClosingService.closePeriod(companyId, yearMonth, user);
        return ResponseEntity.ok(closedPeriod);
    }

    @GetMapping("/periods/closed")
    public ResponseEntity<List<ClosedPeriod>> getClosedPeriods() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        List<ClosedPeriod> closedPeriods = periodClosingService.getClosedPeriods(companyId);
        return ResponseEntity.ok(closedPeriods);
    }

    @GetMapping("/periods/{period}/status")
    public ResponseEntity<PeriodStatusResponse> getPeriodStatus(@PathVariable String period) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth yearMonth = YearMonth.parse(period);
        boolean isClosed = periodClosingService.isPeriodClosed(companyId, yearMonth);
        return ResponseEntity.ok(new PeriodStatusResponse(period, isClosed));
    }

    // ========== Request/Response Records ==========

    public record CreateAccountRequest(
            String code,
            String name,
            AccountType type,
            String description) {
    }

    public record PeriodStatusResponse(
            String period,
            boolean closed) {
    }
}
