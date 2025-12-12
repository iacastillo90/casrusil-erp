package com.casrusil.siierpai.modules.accounting.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.accounting.domain.model.ClosedPeriod;
import com.casrusil.siierpai.modules.accounting.domain.port.out.ClosedPeriodRepository;
import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.ClosedPeriodEntity;
import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.repository.ClosedPeriodJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para periodos cerrados.
 * 
 * <p>
 * Implementa {@link ClosedPeriodRepository} para verificar y registrar
 * el cierre de periodos contables.
 * 
 * @since 1.0
 */
@Component
public class ClosedPeriodJpaAdapter implements ClosedPeriodRepository {

    private final ClosedPeriodJpaRepository jpaRepository;

    public ClosedPeriodJpaAdapter(ClosedPeriodJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ClosedPeriod save(ClosedPeriod closedPeriod) {
        ClosedPeriodEntity entity = toEntity(closedPeriod);
        ClosedPeriodEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ClosedPeriod> findByCompanyIdAndPeriod(CompanyId companyId, YearMonth period) {
        return jpaRepository.findByCompanyIdAndPeriod(companyId.value(), period.toString())
                .map(this::toDomain);
    }

    @Override
    public List<ClosedPeriod> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyIdOrderByPeriodDesc(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(CompanyId companyId, YearMonth period) {
        return jpaRepository.existsByCompanyIdAndPeriod(companyId.value(), period.toString());
    }

    private ClosedPeriodEntity toEntity(ClosedPeriod domain) {
        return new ClosedPeriodEntity(
                domain.getId(),
                domain.getCompanyId().value(),
                domain.getPeriod().toString(),
                domain.getClosedAt(),
                domain.getClosedBy().value(),
                domain.getProfitLoss());
    }

    private ClosedPeriod toDomain(ClosedPeriodEntity entity) {
        return new ClosedPeriod(
                entity.getId(),
                new CompanyId(entity.getCompanyId()),
                YearMonth.parse(entity.getPeriod()),
                entity.getClosedAt(),
                new UserId(entity.getClosedBy()),
                entity.getProfitLoss());
    }
}
