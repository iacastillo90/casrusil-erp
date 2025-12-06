package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

import java.math.BigDecimal;

/**
 * Representa una línea individual dentro de un asiento contable.
 * 
 * <p>
 * Cada línea tiene una cuenta contable asociada y un monto que puede ser
 * Débito (Debe) o Crédito (Haber).
 * 
 * <h2>Invariantes:</h2>
 * <ul>
 * <li>Los montos no pueden ser negativos.</li>
 * <li>Una línea no puede tener Débito y Crédito simultáneamente (uno debe ser
 * cero).</li>
 * </ul>
 * 
 * @param accountCode Código de la cuenta contable (ej: "110101").
 * @param debit       Monto al Debe.
 * @param credit      Monto al Haber.
 * 
 * @see com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry
 * @since 1.0
 */
public record AccountingEntryLine(
        String accountCode,
        BigDecimal debit,
        BigDecimal credit) {
    public AccountingEntryLine {
        if (debit == null)
            debit = BigDecimal.ZERO;
        if (credit == null)
            credit = BigDecimal.ZERO;
        if (debit.compareTo(BigDecimal.ZERO) < 0 || credit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Debit and Credit must be non-negative");
        }
    }

    /**
     * Crea una línea con monto al Debe (Débito).
     * 
     * @param accountCode Código de la cuenta.
     * @param amount      Monto positivo.
     * @return Nueva línea de asiento.
     */
    public static AccountingEntryLine debit(String accountCode, BigDecimal amount) {
        return new AccountingEntryLine(accountCode, amount, BigDecimal.ZERO);
    }

    /**
     * Crea una línea con monto al Haber (Crédito).
     * 
     * @param accountCode Código de la cuenta.
     * @param amount      Monto positivo.
     * @return Nueva línea de asiento.
     */
    public static AccountingEntryLine credit(String accountCode, BigDecimal amount) {
        return new AccountingEntryLine(accountCode, BigDecimal.ZERO, amount);
    }
}
