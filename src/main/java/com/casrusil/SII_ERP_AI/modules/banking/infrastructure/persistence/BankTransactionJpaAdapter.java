package com.casrusil.SII_ERP_AI.modules.banking.infrastructure.persistence;

import com.casrusil.SII_ERP_AI.modules.banking.domain.model.BankTransaction;
import com.casrusil.SII_ERP_AI.modules.banking.domain.port.out.BankTransactionRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador JPA para BankTransactionRepository.
 */
/**
 * Adaptador de persistencia para transacciones bancarias.
 * 
 * <p>
 * Implementa {@link BankTransactionRepository} para el acceso a datos
 * de movimientos bancarios.
 * 
 * @since 1.0
 */
@Component
public class BankTransactionJpaAdapter implements BankTransactionRepository {

    private final BankTransactionJpaRepository jpaRepository;

    public BankTransactionJpaAdapter(BankTransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(BankTransaction transaction) {
        BankTransactionEntity entity = toEntity(transaction);
        jpaRepository.save(entity);
    }

    @Override
    public List<BankTransaction> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<BankTransaction> findByCompanyIdAndDateRange(CompanyId companyId, LocalDate startDate,
            LocalDate endDate) {
        return jpaRepository.findByCompanyIdAndDateBetween(companyId.value(), startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<BankTransaction> findUnreconciledByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyIdAndReconciledFalse(companyId.value())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BankTransaction findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain)
                .orElse(null);
    }

    private BankTransactionEntity toEntity(BankTransaction transaction) {
        return new BankTransactionEntity(
                transaction.getId(),
                transaction.getCompanyId().value(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getReference(),
                transaction.isReconciled(),
                transaction.getReconciledWithEntryId());
    }

    private BankTransaction toDomain(BankTransactionEntity entity) {
        BankTransaction transaction = new BankTransaction(
                entity.getId(),
                new CompanyId(entity.getCompanyId()),
                entity.getDate(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getReference());

        if (entity.isReconciled() && entity.getReconciledWithEntryId() != null) {
            transaction.markAsReconciled(entity.getReconciledWithEntryId());
        }

        return transaction;
    }
}
