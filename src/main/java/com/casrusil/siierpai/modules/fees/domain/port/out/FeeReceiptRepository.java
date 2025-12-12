package com.casrusil.siierpai.modules.fees.domain.port.out;

import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeeReceiptRepository {
    void save(FeeReceipt feeReceipt);

    void saveAll(List<FeeReceipt> feeReceipts);

    List<FeeReceipt> findByCompanyIdAndIssueDateBetween(CompanyId companyId, LocalDate startDate, LocalDate endDate);

    List<FeeReceipt> findByCompanyIdAndYear(CompanyId companyId, int year);

    Optional<FeeReceipt> findByCompanyIdAndFolioAndIssuerRut(CompanyId companyId, Long folio, String issuerRut);
}
