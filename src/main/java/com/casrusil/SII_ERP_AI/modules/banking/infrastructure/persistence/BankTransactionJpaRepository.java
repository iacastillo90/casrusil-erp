package com.casrusil.SII_ERP_AI.modules.banking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para BankTransactionEntity.
 */
/**
 * Repositorio JPA para transacciones bancarias.
 * 
 * <p>
 * Gestiona la persistencia de los movimientos bancarios y facilita
 * consultas para la conciliación.
 * 
 * @since 1.0
 */
@Repository
public interface BankTransactionJpaRepository extends JpaRepository<BankTransactionEntity, UUID> {
    List<BankTransactionEntity> findByCompanyId(UUID companyId);

    List<BankTransactionEntity> findByCompanyIdAndDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<BankTransactionEntity> findByCompanyIdAndReconciledFalse(UUID companyId);
}
