package com.casrusil.siierpai.modules.banking.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.banking.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.banking.domain.model.ReconciliationMatch;
import com.casrusil.siierpai.modules.banking.domain.port.out.BankTransactionRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de conciliación bancaria mejorado (Nivel Dios).
 * Implementa lógica heurística avanzada: RUT Match, Tolerancia Asimétrica,
 * Abonos.
 */
@Service
public class ReconciliationService {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationService.class);
    private final BankTransactionRepository bankTransactionRepository;
    private final AccountingEntryRepository accountingEntryRepository;

    // Tolerancia Asimétrica: Permitimos hasta 60 días de atraso en el pago (Factura
    // Ene -> Pago Mar)
    private static final int MAX_DAYS_AFTER = 60;
    // Pero bloqueamos pagos muy anticipados (máximo 2 días antes por errores de
    // fecha)
    private static final int MAX_DAYS_BEFORE = 2;

    // Regex para detectar RUTs en descripciones (Formatos: 12.345.678-9,
    // 12345678-9, 12345678)
    private static final Pattern RUT_PATTERN = Pattern.compile("\\b(\\d{1,3}(?:\\.?\\d{3})*)-?([\\dkK])\\b");

    public ReconciliationService(BankTransactionRepository bankTransactionRepository,
            AccountingEntryRepository accountingEntryRepository) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.accountingEntryRepository = accountingEntryRepository;
    }

    /**
     * Encuentra coincidencias automáticas.
     */
    @Transactional(readOnly = true)
    public List<ReconciliationMatch> findMatches(CompanyId companyId) {
        List<ReconciliationMatch> matches = new ArrayList<>();

        List<BankTransaction> unreconciledTransactions = bankTransactionRepository
                .findUnreconciledByCompanyId(companyId);

        List<AccountingEntry> accountingEntries = accountingEntryRepository.findByCompanyId(companyId);

        logger.info("Buscando matches para {} transacciones bancarias contra {} asientos contables",
                unreconciledTransactions.size(), accountingEntries.size());

        for (BankTransaction transaction : unreconciledTransactions) {
            ReconciliationMatch bestMatch = findBestMatch(transaction, accountingEntries);
            if (bestMatch != null && bestMatch.isHighConfidence()) {
                matches.add(bestMatch);
            }
        }

        return matches;
    }

    public void applyReconciliation(ReconciliationMatch match) {
        BankTransaction transaction = bankTransactionRepository.findById(match.getBankTransactionId());
        if (transaction != null && !transaction.isReconciled()) {
            transaction.markAsReconciled(match.getAccountingEntryId());
            bankTransactionRepository.save(transaction);
        }
    }

    public void undoReconciliation(java.util.UUID transactionId) {
        BankTransaction transaction = bankTransactionRepository.findById(transactionId);
        if (transaction != null && transaction.isReconciled()) {
            transaction.unreconcile();
            bankTransactionRepository.save(transaction);
        }
    }

    private ReconciliationMatch findBestMatch(BankTransaction transaction, List<AccountingEntry> entries) {
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

    private double calculateMatchScore(BankTransaction transaction, AccountingEntry entry) {
        double score = 0.0;
        BigDecimal transactionAmount = transaction.getAmount().abs();
        BigDecimal entryMagnitude = calculateEntryMagnitude(entry);

        // 1. ANÁLISIS DE FECHAS (ASIMÉTRICO) - CRÍTICO
        // Fecha Transacción (Pago) vs Fecha Asiento (Factura)
        long daysDiff = ChronoUnit.DAYS.between(entry.getEntryDate(), transaction.getDate());

        // Regla: El pago DEBE ser posterior o igual a la factura (con mínima tolerancia
        // de error)
        // daysDiff > 0: Pago posterior a factura. daysDiff < 0: Pago anterior.
        if (daysDiff < -MAX_DAYS_BEFORE || daysDiff > MAX_DAYS_AFTER) {
            return 0.0; // Fuera de rango temporal válido
        }

        // 2. BUSQUEDA POR RUT (NIVEL DIOS)
        String rutsInDescription = extractRut(transaction.getDescription());
        String entryTaxPayer = entry.getTaxPayerId() != null ? normalizeRut(entry.getTaxPayerId()) : "";
        boolean rutMatch = false;

        if (!entryTaxPayer.isEmpty() && rutsInDescription != null) {
            if (rutsInDescription.contains(entryTaxPayer)) {
                score += 0.4; // Gran impulso si el RUT está explícito en el banco
                rutMatch = true;
            }
        }

        // 3. ANÁLISIS DE MONTO (COINCIDENCIA EXACTA O ABONO)
        BigDecimal diffAmount = transactionAmount.subtract(entryMagnitude).abs();
        boolean exactAmountMatch = diffAmount.doubleValue() < 10.0; // Tolerancia $10 pesos

        // Detectar ABONO: Monto Banco <= Monto Factura y RUT coincide o Descripción muy
        // similar
        boolean isPartialPayment = !exactAmountMatch &&
                transactionAmount.compareTo(entryMagnitude) <= 0 &&
                (rutMatch || calculateStringSimilarity(transaction.getDescription(), entry.getDescription()) > 0.6);

        if (exactAmountMatch) {
            score += 0.5;
        } else if (isPartialPayment) {
            score += 0.3; // Puntaje menor pero válido para sugerencia
        } else {
            return 0.0; // Si no calza monto ni es abono validado por RUT, descartar
        }

        // 4. FECHA SCORE DETALLADO
        // Preferimos pagos cercanos, pero aceptamos lejanos
        double dateScore = 0.0;
        if (Math.abs(daysDiff) <= 5) {
            dateScore = 0.1; // Muy cercano (Bonus)
        } else {
            // Penalización leve por lejanía, pero no eliminación
            dateScore = 0.1 * (1.0 - (double) Math.abs(daysDiff) / MAX_DAYS_AFTER);
        }
        score += Math.max(0, dateScore);

        return Math.min(score, 1.0);
    }

    private BigDecimal calculateEntryMagnitude(AccountingEntry entry) {
        BigDecimal debit = entry.getLines().stream()
                .map(AccountingEntryLine::debit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debit.compareTo(BigDecimal.ZERO) != 0)
            return debit;

        return entry.getLines().stream()
                .map(AccountingEntryLine::credit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String extractRut(String text) {
        if (text == null)
            return null;
        Matcher matcher = RUT_PATTERN.matcher(text);
        if (matcher.find()) {
            String rut = matcher.group(1).replace(".", "") + "-" + matcher.group(2).toUpperCase();
            return rut;
        }
        return null; // No RUT found
    }

    private String normalizeRut(String rut) {
        if (rut == null)
            return "";
        return rut.replace(".", "").toUpperCase();
    }

    private double calculateStringSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null)
            return 0.0;
        String str1 = s1.toLowerCase();
        String str2 = s2.toLowerCase();
        if (str1.contains(str2) || str2.contains(str1))
            return 0.8;
        return 0.0; // Simplificado para rendimiento
    }

    private String buildMatchReason(BankTransaction transaction, AccountingEntry entry, double score) {
        long diff = ChronoUnit.DAYS.between(entry.getEntryDate(), transaction.getDate());
        return String.format(
                "Score: %.2f. Monto Banco: %s vs Factura: %s. Días dif: %d. RUT Match: %s",
                score,
                transaction.getAmount(),
                calculateEntryMagnitude(entry),
                diff,
                extractRut(transaction.getDescription()) != null ? "SÍ" : "NO");
    }
}
