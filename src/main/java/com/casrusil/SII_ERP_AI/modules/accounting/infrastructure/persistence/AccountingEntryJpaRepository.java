package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence;

import com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.entity.AccountingEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AccountingEntryJpaRepository extends JpaRepository<AccountingEntryEntity, UUID> {
    List<AccountingEntryEntity> findByCompanyId(UUID companyId);
}
