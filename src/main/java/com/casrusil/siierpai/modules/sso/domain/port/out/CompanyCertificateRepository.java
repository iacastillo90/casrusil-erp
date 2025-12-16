package com.casrusil.siierpai.modules.sso.domain.port.out;

import com.casrusil.siierpai.modules.sso.domain.model.CompanyCertificate;
import java.util.Optional;
import java.util.UUID;

public interface CompanyCertificateRepository {
    Optional<CompanyCertificate> findByCompanyId(UUID companyId);

    CompanyCertificate save(CompanyCertificate certificate);
}
