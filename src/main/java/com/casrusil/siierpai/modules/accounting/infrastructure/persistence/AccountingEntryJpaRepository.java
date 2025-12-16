package com.casrusil.siierpai.modules.accounting.infrastructure.persistence;

import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.AccountingEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;

public interface AccountingEntryJpaRepository extends JpaRepository<AccountingEntryEntity, UUID> {
    List<AccountingEntryEntity> findByCompanyId(UUID companyId);

    @Query("""
                SELECT new com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement(
                    e.id,
                    e.occurredOn,
                    e.description,
                    l.debit,
                    l.credit,
                    CAST(0 AS BigDecimal)
                )
                FROM AccountingEntryEntity e
                JOIN e.lines l
                WHERE e.companyId = :companyId
                AND l.accountCode = :accountCode
                AND e.occurredOn BETWEEN :startDate AND :endDate
                ORDER BY e.occurredOn ASC
            """)
    List<AccountMovement> findMovementsByAccount(
            @Param("companyId") UUID companyId,
            @Param("accountCode") String accountCode,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    void deleteByCompanyIdAndReferenceTypeAndOccurredOnBetween(UUID companyId, String referenceType, Instant startDate,
            Instant endDate);
}
