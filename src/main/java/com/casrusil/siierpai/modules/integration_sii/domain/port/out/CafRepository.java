package com.casrusil.siierpai.modules.integration_sii.domain.port.out;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.Optional;

public interface CafRepository {
    void save(CompanyId companyId, Caf caf);

    Optional<Caf> findActiveForFolio(CompanyId companyId, String tipoDte, Long folio);
}
