package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.adapter;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.Account;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.entity.AccountEntity;
import com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.repository.AccountJpaRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountJpaAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    public AccountJpaAdapter(AccountJpaRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = toEntity(account);
        AccountEntity savedEntity = accountJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findByCode(CompanyId companyId, String code) {
        return accountJpaRepository.findByCompanyIdAndCode(companyId.value(), code)
                .map(this::toDomain);
    }

    @Override
    public List<Account> findAll(CompanyId companyId) {
        return accountJpaRepository.findByCompanyId(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AccountEntity toEntity(Account account) {
        return new AccountEntity(
                account.getId(),
                account.getCompanyId().value(),
                account.getCode(),
                account.getName(),
                account.getType(),
                account.getDescription(),
                account.isActive());
    }

    private Account toDomain(AccountEntity entity) {
        return new Account(
                entity.getId(),
                new CompanyId(entity.getCompanyId()),
                entity.getCode(),
                entity.getName(),
                entity.getType(),
                entity.getDescription(),
                entity.isActive());
    }
}
