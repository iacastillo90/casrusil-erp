package com.casrusil.siierpai.modules.banking.infrastructure.web;

import com.casrusil.siierpai.modules.banking.application.service.BankStatementParser;
import com.casrusil.siierpai.modules.banking.application.service.ReconciliationService;
import com.casrusil.siierpai.modules.banking.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.banking.domain.model.ReconciliationMatch;
import com.casrusil.siierpai.modules.banking.domain.port.out.BankTransactionRepository;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para conciliación bancaria.
 */
@RestController
@RequestMapping("/api/v1/bank")
public class BankStatementController {

    private final BankStatementParser parser;
    private final ReconciliationService reconciliationService;
    private final BankTransactionRepository bankTransactionRepository;

    public BankStatementController(BankStatementParser parser,
            ReconciliationService reconciliationService,
            BankTransactionRepository bankTransactionRepository) {
        this.parser = parser;
        this.reconciliationService = reconciliationService;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    /**
     * Sube un extracto bancario (CSV o Excel) y lo parsea.
     * 
     * POST /api/v1/bank/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadBankStatement(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            List<BankTransaction> transactions;

            if (filename != null && filename.endsWith(".csv")) {
                transactions = parser.parseCsv(file.getInputStream(), CompanyContext.requireCompanyId());
            } else if (filename != null
                    && (filename.endsWith(".xlsx") || filename.endsWith(".xls") || filename.endsWith(".xlsb"))) {
                transactions = parser.parseExcel(file.getInputStream(), CompanyContext.requireCompanyId());
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported file format. Use CSV or Excel."));
            }

            // Save all transactions
            for (BankTransaction transaction : transactions) {
                bankTransactionRepository.save(transaction);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bank statement uploaded successfully");
            response.put("transactionsCount", transactions.size());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error parsing file: " + e.getMessage()));
        }
    }

    /**
     * Obtiene el estado de conciliación (transacciones no conciliadas y sugerencias
     * de match).
     * 
     * GET /api/v1/bank/reconciliation
     */
    @GetMapping("/reconciliation")
    public ResponseEntity<Map<String, Object>> getReconciliationStatus() {
        List<BankTransaction> unreconciledTransactions = bankTransactionRepository
                .findUnreconciledByCompanyId(CompanyContext.requireCompanyId());

        List<ReconciliationMatch> suggestedMatches = reconciliationService
                .findMatches(CompanyContext.requireCompanyId());

        Map<String, Object> response = new HashMap<>();
        response.put("unreconciledCount", unreconciledTransactions.size());
        response.put("unreconciledTransactions", unreconciledTransactions);
        response.put("suggestedMatches", suggestedMatches);

        return ResponseEntity.ok(response);
    }

    /**
     * Aplica una conciliación sugerida.
     * 
     * POST /api/v1/bank/reconciliation/apply
     */
    @PostMapping("/reconciliation/apply")
    public ResponseEntity<Map<String, String>> applyReconciliation(@RequestBody ReconciliationMatchRequest request) {
        ReconciliationMatch match = new ReconciliationMatch(
                request.bankTransactionId(),
                request.accountingEntryId(),
                1.0, // Manual match has full confidence
                "Manual reconciliation");

        reconciliationService.applyReconciliation(match);

        return ResponseEntity.ok(Map.of("message", "Reconciliation applied successfully"));
    }

    /**
     * Deshace una conciliación.
     * 
     * DELETE /api/v1/bank/reconciliation/{transactionId}
     */
    @DeleteMapping("/reconciliation/{transactionId}")
    public ResponseEntity<Map<String, String>> undoReconciliation(@PathVariable UUID transactionId) {
        reconciliationService.undoReconciliation(transactionId);
        return ResponseEntity.ok(Map.of("message", "Reconciliation undone successfully"));
    }

    /**
     * DTO para aplicar conciliación manual.
     */
    public record ReconciliationMatchRequest(UUID bankTransactionId, UUID accountingEntryId) {
    }
}
