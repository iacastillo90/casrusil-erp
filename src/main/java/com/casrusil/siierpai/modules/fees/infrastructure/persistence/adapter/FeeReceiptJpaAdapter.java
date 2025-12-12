package com.casrusil.siierpai.modules.fees.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.modules.fees.domain.port.out.FeeReceiptRepository;
import com.casrusil.siierpai.modules.fees.infrastructure.persistence.entity.FeeReceiptEntity;
import com.casrusil.siierpai.modules.fees.infrastructure.persistence.repository.FeeReceiptJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FeeReceiptJpaAdapter implements FeeReceiptRepository {

    private final FeeReceiptJpaRepository jpaRepository;

    public FeeReceiptJpaAdapter(FeeReceiptJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(FeeReceipt feeReceipt) {
        jpaRepository.save(toEntity(feeReceipt));
    }

    @Override
    public void saveAll(List<FeeReceipt> feeReceipts) {
        jpaRepository.saveAll(feeReceipts.stream().map(this::toEntity).collect(Collectors.toList()));
    }

    @Override
    public List<FeeReceipt> findByCompanyIdAndIssueDateBetween(CompanyId companyId, LocalDate startDate,
            LocalDate endDate) {
        return jpaRepository.findByCompanyIdAndIssueDateBetween(companyId.getValue(), startDate, endDate)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<FeeReceipt> findByCompanyIdAndYear(CompanyId companyId, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return findByCompanyIdAndIssueDateBetween(companyId, start, end);
    }

    @Override
    public Optional<FeeReceipt> findByCompanyIdAndFolioAndIssuerRut(CompanyId companyId, Long folio, String issuerRut) {
        return jpaRepository.findByCompanyIdAndFolioAndIssuerRut(companyId.getValue(), folio, issuerRut)
                .map(this::toDomain);
    }

    private FeeReceiptEntity toEntity(FeeReceipt domain) {
        return new FeeReceiptEntity(
                domain.getId(),
                domain.getCompanyId().getValue(),
                domain.getFolio(),
                domain.getIssuerRut(),
                domain.getReceiverRut(),
                domain.getIssuerName(),
                domain.getIssueDate(),
                domain.getGrossAmount(),
                domain.getRetentionAmount(),
                domain.getNetAmount(),
                FeeReceiptEntity.Status.valueOf(domain.getStatus().name()));
    }

    private FeeReceipt toDomain(FeeReceiptEntity entity) {
        return new FeeReceipt(
                entity.getId(),
                new CompanyId(entity.getCompanyId()),
                entity.getFolio(),
                entity.getIssuerRut(),
                entity.getReceiverRut(),
                entity.getIssuerName(),
                entity.getIssueDate(),
                entity.getGrossAmount(),
                entity.getRetentionAmount(),
                entity.getNetAmount(),
                FeeReceipt.Status.valueOf(entity.getStatus().name()));
    }
}
