package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.entity.AccountingEntryEntity;
import com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.entity.AccountingEntryLineEmbeddable;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para asientos contables.
 * 
 * <p>
 * Implementa la interfaz del dominio {@link AccountingEntryRepository} usando
 * JPA.
 * Realiza el mapeo entre entidades de dominio y entidades JPA.
 * 
 * @see AccountingEntryRepository
 * @see AccountingEntryJpaRepository
 * @since 1.0
 */
@Component
public class AccountingEntryJpaAdapter implements AccountingEntryRepository {

        private final AccountingEntryJpaRepository jpaRepository;

        public AccountingEntryJpaAdapter(AccountingEntryJpaRepository jpaRepository) {
                this.jpaRepository = jpaRepository;
        }

        @Override
        public void save(AccountingEntry entry) {
                AccountingEntryEntity entity = toEntity(entry);
                jpaRepository.save(entity);
        }

        @Override
        public List<AccountingEntry> findByCompanyId(CompanyId companyId) {
                return jpaRepository.findByCompanyId(companyId.value()).stream()
                                .map(this::toDomain)
                                .collect(Collectors.toList());
        }

        private AccountingEntryEntity toEntity(AccountingEntry entry) {
                List<AccountingEntryLineEmbeddable> lineEntities = entry.getLines().stream()
                                .map(line -> new AccountingEntryLineEmbeddable(
                                                line.accountCode(),
                                                line.debit(),
                                                line.credit()))
                                .collect(Collectors.toList());

                return new AccountingEntryEntity(
                                entry.getId(),
                                entry.getCompanyId().value(),
                                entry.getOccurredOn(),
                                entry.getDescription(),
                                entry.getReferenceId(),
                                entry.getReferenceType(),
                                lineEntities,
                                entry.getType());
        }

        private AccountingEntry toDomain(AccountingEntryEntity entity) {
                List<AccountingEntryLine> lines = entity.getLines().stream()
                                .map(line -> new AccountingEntryLine(
                                                line.getAccountCode(),
                                                line.getDebit(),
                                                line.getCredit()))
                                .collect(Collectors.toList());

                return new AccountingEntry(
                                new CompanyId(entity.getCompanyId()),
                                entity.getDescription(),
                                entity.getReferenceId(),
                                entity.getReferenceType(),
                                lines,
                                entity.getType());
        }
}
