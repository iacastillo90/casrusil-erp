package com.casrusil.siierpai.modules.accounting.domain.port.out;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findByCode(CompanyId companyId, String code);

    List<Account> findAll(CompanyId companyId);
}
