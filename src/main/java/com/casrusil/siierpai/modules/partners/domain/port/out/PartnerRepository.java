package com.casrusil.siierpai.modules.partners.domain.port.out;

import com.casrusil.siierpai.modules.partners.domain.model.Partner;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.util.Optional;

public interface PartnerRepository {
    Partner save(Partner partner);

    Optional<Partner> findByCompanyIdAndRut(CompanyId companyId, String rut);
}
