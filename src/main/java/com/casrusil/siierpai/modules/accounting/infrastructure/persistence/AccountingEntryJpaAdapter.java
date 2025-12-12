
package com.casrusil.siierpai.modules.accounting.infrastructure.persistence;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.AccountingEntryEntity;
import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.AccountingEntryLineEmbeddable;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
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
        private final com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository accountRepository;

        public AccountingEntryJpaAdapter(AccountingEntryJpaRepository jpaRepository,
                        com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository accountRepository) {
                this.jpaRepository = jpaRepository;
                this.accountRepository = accountRepository;
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

        @Override
        public List<com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement> findMovementsByAccount(
                        CompanyId companyId, String accountCode, java.time.LocalDate from, java.time.LocalDate to) {
                java.time.Instant startDate = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
                java.time.Instant endDate = to.atTime(java.time.LocalTime.MAX).atZone(ZoneId.systemDefault())
                                .toInstant();

                return jpaRepository.findMovementsByAccount(companyId.value(), accountCode, startDate, endDate);
        }

        private AccountingEntryEntity toEntity(AccountingEntry entry) {
                List<AccountingEntryLineEmbeddable> lineEntities = entry.getLines().stream()
                                .map(line -> new AccountingEntryLineEmbeddable(
                                                line.accountCode(),
                                                line.debit(),
                                                line.credit()))
                                .collect(Collectors.toList());

                // Convert LocalDate (Domain) to Instant (Entity)
                // We use start of day at Chile zone to ensure correct date persistence
                java.time.ZoneId chileZone = java.time.ZoneId.of("America/Santiago");
                java.time.Instant occurredOn = entry.getEntryDate().atStartOfDay(chileZone).toInstant();

                return new AccountingEntryEntity(
                                entry.getId(),
                                entry.getCompanyId().value(),
                                occurredOn,
                                entry.getDescription(),
                                entry.getReferenceId(),
                                entry.getReferenceType(),
                                entry.getTaxPayerId(),
                                entry.getTaxPayerName(),
                                entry.getDocumentType(),
                                entry.getDocumentNumber(),
                                entry.getStatus(),
                                lineEntities,
                                entry.getType());
        }

        private AccountingEntry toDomain(AccountingEntryEntity entity) {
                List<AccountingEntryLine> lines = entity.getLines().stream()
                                .map(line -> {
                                        String accountName = accountRepository
                                                        .findByCode(new CompanyId(entity.getCompanyId()),
                                                                        line.getAccountCode())
                                                        .map(com.casrusil.siierpai.modules.accounting.domain.model.Account::getName)
                                                        .orElse("Unknown");
                                        return new AccountingEntryLine(
                                                        line.getAccountCode(),
                                                        accountName,
                                                        line.getDebit(),
                                                        line.getCredit());
                                })
                                .collect(Collectors.toList());

                // Convert Instant (Entity) to LocalDate (Domain)
                java.time.ZoneId chileZone = java.time.ZoneId.of("America/Santiago");
                java.time.LocalDate entryDate = java.time.LocalDate.ofInstant(entity.getOccurredOn(),
                                chileZone);

                return new AccountingEntry(
                                new CompanyId(entity.getCompanyId()),
                                entryDate,
                                entity.getDescription(),
                                entity.getReferenceId(),
                                entity.getReferenceType(),
                                entity.getTaxPayerId(),
                                entity.getTaxPayerName(),
                                entity.getDocumentType(),
                                entity.getDocumentNumber(),
                                entity.getStatus(),
                                lines,
                                entity.getType());
        }
}
