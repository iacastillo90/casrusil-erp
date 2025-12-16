package com.casrusil.siierpai.modules.sso.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.sso.domain.model.CompanyCertificate;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyCertificateRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyCertificateJpaRepository
        extends JpaRepository<CompanyCertificate, UUID>, CompanyCertificateRepository {
    Optional<CompanyCertificate> findByCompanyId(UUID companyId);
}
