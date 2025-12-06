package com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.Account;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findByCode(CompanyId companyId, String code);

    List<Account> findAll(CompanyId companyId);
}
