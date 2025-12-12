package com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.ClosedPeriod;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.accounting.domain.service.PeriodClosingService;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import java.time.LocalDate;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
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
    private final com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService openingBalanceService;
    private final com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService accountingEntryService;
    private final com.casrusil.siierpai.modules.accounting.infrastructure.parser.BalanceSheetParser balanceSheetParser;
    private final BalanceSheetService balanceSheetService;

    public AccountingController(AccountingEntryRepository accountingEntryRepository,
            AccountRepository accountRepository,
            F29CalculatorService f29CalculatorService,
            PeriodClosingService periodClosingService,
            com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService openingBalanceService,
            com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService accountingEntryService,
            com.casrusil.siierpai.modules.accounting.infrastructure.parser.BalanceSheetParser balanceSheetParser,
            BalanceSheetService balanceSheetService) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountRepository = accountRepository;
        this.f29CalculatorService = f29CalculatorService;
        this.periodClosingService = periodClosingService;
        this.openingBalanceService = openingBalanceService;
        this.accountingEntryService = accountingEntryService;
        this.balanceSheetParser = balanceSheetParser;
        this.balanceSheetService = balanceSheetService;
    }

    @PostMapping("/opening-balance/upload")
    public ResponseEntity<java.util.Map<String, Object>> uploadOpeningBalance(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        try {
            List<com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService.OpeningBalanceItem> items = balanceSheetParser
                    .parseTsv(file.getInputStream());
            openingBalanceService.setOpeningBalance(companyId, items);
            return ResponseEntity
                    .ok(java.util.Map.of("message", "Opening balance imported successfully", "count", items.size()));
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("error", "Failed to parse file: " + e.getMessage()));
        }
    }

    @PostMapping("/opening-balance")
    public ResponseEntity<Void> setOpeningBalance(
            @RequestBody List<com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService.OpeningBalanceItem> items) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        openingBalanceService.setOpeningBalance(companyId, items);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/entries")
    public ResponseEntity<List<AccountingEntry>> getEntries() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(accountingEntryRepository.findByCompanyId(companyId));
    }

    @GetMapping("/ledger/{accountCode}")
    public ResponseEntity<List<com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement>> getLedger(
            @PathVariable String accountCode,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        List<com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement> ledger = accountingEntryService
                .getLedger(companyId, accountCode, from, to);
        return ResponseEntity.ok(ledger);
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

    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetReport> getBalanceSheet(@RequestParam String date) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        LocalDate reportDate = LocalDate.parse(date);
        BalanceSheetReport report = balanceSheetService.generateBalanceSheet(companyId, reportDate);
        return ResponseEntity.ok(report);
    }

    // ========== F29 Endpoints ==========

    @GetMapping("/f29")
    public ResponseEntity<F29Report> calculateF29(@RequestParam String period) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth yearMonth = parsePeriod(period);
        F29Report report = f29CalculatorService.calculateF29(companyId, yearMonth);
        return ResponseEntity.ok(report);
    }

    // ========== Period Closing Endpoints ==========

    @PostMapping("/periods/close")
    public ResponseEntity<ClosedPeriod> closePeriod(
            @RequestParam String period,
            @RequestParam String userId) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth yearMonth = parsePeriod(period);
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
        YearMonth yearMonth = parsePeriod(period);
        boolean isClosed = periodClosingService.isPeriodClosed(companyId, yearMonth);
        return ResponseEntity.ok(new PeriodStatusResponse(period, isClosed));
    }

    // ========== Helper Methods ==========

    private YearMonth parsePeriod(String period) {
        if (period.contains("-")) {
            return YearMonth.parse(period);
        } else {
            return YearMonth.parse(period, java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        }
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
