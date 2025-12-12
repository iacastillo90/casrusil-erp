package com.casrusil.siierpai.modules.accounting.domain.port.out;

import com.casrusil.siierpai.modules.accounting.domain.model.ClosedPeriod;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface ClosedPeriodRepository {
    ClosedPeriod save(ClosedPeriod closedPeriod);

    Optional<ClosedPeriod> findByCompanyIdAndPeriod(CompanyId companyId, YearMonth period);

    List<ClosedPeriod> findByCompanyId(CompanyId companyId);

    boolean exists(CompanyId companyId, YearMonth period);
}
