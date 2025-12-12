package com.casrusil.siierpai.modules.fees.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.fees.infrastructure.persistence.entity.FeeReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeeReceiptJpaRepository extends JpaRepository<FeeReceiptEntity, UUID> {
    List<FeeReceiptEntity> findByCompanyIdAndIssueDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<FeeReceiptEntity> findByCompanyId(UUID companyId);

    Optional<FeeReceiptEntity> findByCompanyIdAndFolioAndIssuerRut(UUID companyId, Long folio, String issuerRut);
}
