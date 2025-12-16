package com.casrusil.siierpai.modules.partners.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.partners.infrastructure.persistence.entity.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PartnerJpaRepository extends JpaRepository<PartnerEntity, UUID> {
    Optional<PartnerEntity> findByCompanyIdAndRut(UUID companyId, String rut);
}
