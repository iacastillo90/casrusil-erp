package com.casrusil.SII_ERP_AI.modules.banking.application.service;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.SII_ERP_AI.modules.banking.domain.model.BankTransaction;
import com.casrusil.SII_ERP_AI.modules.banking.domain.model.ReconciliationMatch;
import com.casrusil.SII_ERP_AI.modules.banking.domain.port.out.BankTransactionRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de conciliación bancaria.
 * Implementa lógica de auto-matching entre transacciones bancarias y asientos
 * contables.
 */
@Service
public class ReconciliationService {

    private final BankTransactionRepository bankTransactionRepository;
    private final AccountingEntryRepository accountingEntryRepository;

    private static final int DATE_TOLERANCE_DAYS = 3;
    private static final double DESCRIPTION_SIMILARITY_THRESHOLD = 0.6;

    public ReconciliationService(BankTransactionRepository bankTransactionRepository,
            AccountingEntryRepository accountingEntryRepository) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.accountingEntryRepository = accountingEntryRepository;
    }

    /**
     * Encuentra coincidencias automáticas para transacciones bancarias no
     * conciliadas.
     * 
     * @param companyId ID de la empresa
     * @return Lista de coincidencias encontradas
     */
    public List<ReconciliationMatch> findMatches(CompanyId companyId) {
        List<ReconciliationMatch> matches = new ArrayList<>();

        List<BankTransaction> unreconciledTransactions = bankTransactionRepository
                .findUnreconciledByCompanyId(companyId);

        List<AccountingEntry> accountingEntries = accountingEntryRepository.findByCompanyId(companyId);

        for (BankTransaction transaction : unreconciledTransactions) {
            ReconciliationMatch bestMatch = findBestMatch(transaction, accountingEntries);
            if (bestMatch != null && bestMatch.isHighConfidence()) {
                matches.add(bestMatch);
            }
        }

        return matches;
    }

    /**
     * Aplica una conciliación, marcando la transacción como conciliada.
     * 
     * @param match Coincidencia a aplicar
     */
    public void applyReconciliation(ReconciliationMatch match) {
        BankTransaction transaction = bankTransactionRepository.findById(match.getBankTransactionId());
        if (transaction != null && !transaction.isReconciled()) {
            transaction.markAsReconciled(match.getAccountingEntryId());
            bankTransactionRepository.save(transaction);
        }
    }

    /**
     * Deshace una conciliación.
     * 
     * @param transactionId ID de la transacción bancaria
     */
    public void undoReconciliation(java.util.UUID transactionId) {
        BankTransaction transaction = bankTransactionRepository.findById(transactionId);
        if (transaction != null && transaction.isReconciled()) {
            transaction.unreconcile();
            bankTransactionRepository.save(transaction);
        }
    }

    /**
     * Encuentra la mejor coincidencia para una transacción bancaria.
     */
    private ReconciliationMatch findBestMatch(BankTransaction transaction,
            List<AccountingEntry> entries) {
        ReconciliationMatch bestMatch = null;
        double bestScore = 0.0;

        for (AccountingEntry entry : entries) {
            double score = calculateMatchScore(transaction, entry);
            if (score > bestScore) {
                bestScore = score;
                String reason = buildMatchReason(transaction, entry, score);
                bestMatch = new ReconciliationMatch(
                        transaction.getId(),
                        entry.getId(),
                        score,
                        reason);
            }
        }

        return bestMatch;
    }

    /**
     * Calcula un score de coincidencia entre una transacción y un asiento.
     * Score de 0.0 a 1.0, donde 1.0 es coincidencia perfecta.
     */
    private double calculateMatchScore(BankTransaction transaction, AccountingEntry entry) {
        double score = 0.0;

        // 1. Verificar coincidencia de monto (peso: 50%)
        BigDecimal transactionAmount = transaction.getAmount().abs();
        BigDecimal entryTotalAmount = entry.getLines().stream()
                .map(line -> line.debit().add(line.credit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (transactionAmount.compareTo(entryTotalAmount) == 0) {
            score += 0.5;
        } else {
            return 0.0; // Si el monto no coincide exactamente, no es un match válido
        }

        // 2. Verificar proximidad de fecha (peso: 30%)
        LocalDate transactionDate = transaction.getDate();
        LocalDate entryDate = entry.getOccurredOn()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        long daysDifference = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(transactionDate, entryDate));
        if (daysDifference <= DATE_TOLERANCE_DAYS) {
            double dateScore = 1.0 - (daysDifference / (double) DATE_TOLERANCE_DAYS);
            score += 0.3 * dateScore;
        }

        // 3. Verificar similitud de descripción (peso: 20%)
        double descriptionSimilarity = calculateStringSimilarity(
                transaction.getDescription(),
                entry.getDescription());
        if (descriptionSimilarity >= DESCRIPTION_SIMILARITY_THRESHOLD) {
            score += 0.2 * descriptionSimilarity;
        }

        return score;
    }

    /**
     * Calcula similitud entre dos strings usando Levenshtein distance.
     */
    private double calculateStringSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        String str1 = s1.toLowerCase();
        String str2 = s2.toLowerCase();

        int maxLen = Math.max(str1.length(), str2.length());
        if (maxLen == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance(str1, str2);
        return 1.0 - (distance / (double) maxLen);
    }

    /**
     * Calcula la distancia de Levenshtein entre dos strings.
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Construye una descripción del motivo de coincidencia.
     */
    private String buildMatchReason(BankTransaction transaction, AccountingEntry entry, double score) {
        return String.format(
                "Match Score: %.2f | Amount: %s | Date Diff: %d days | Desc: '%s' vs '%s'",
                score,
                transaction.getAmount(),
                Math.abs(java.time.temporal.ChronoUnit.DAYS.between(
                        transaction.getDate(),
                        entry.getOccurredOn().atZone(java.time.ZoneId.systemDefault()).toLocalDate())),
                transaction.getDescription(),
                entry.getDescription());
    }
}
