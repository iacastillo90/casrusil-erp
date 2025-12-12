package com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class AccountingEntryLineEmbeddable {

    @Column(name = "account_code", nullable = false)
    private String accountCode;

    @Column(nullable = false)
    private BigDecimal debit;

    @Column(nullable = false)
    private BigDecimal credit;

    public AccountingEntryLineEmbeddable() {
    }

    public AccountingEntryLineEmbeddable(String accountCode, BigDecimal debit, BigDecimal credit) {
        this.accountCode = accountCode;
        this.debit = debit;
        this.credit = credit;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }
}
