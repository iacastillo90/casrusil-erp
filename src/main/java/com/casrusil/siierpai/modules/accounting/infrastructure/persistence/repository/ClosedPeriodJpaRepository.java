package com.casrusil.siierpai.modules.accounting.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.ClosedPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para periodos cerrados.
 * 
 * <p>
 * Permite consultar si un periodo específico (mes/año) está cerrado
 * para una empresa determinada.
 * 
 * @since 1.0
 */
@Repository
public interface ClosedPeriodJpaRepository extends JpaRepository<ClosedPeriodEntity, UUID> {
    Optional<ClosedPeriodEntity> findByCompanyIdAndPeriod(UUID companyId, String period);

    List<ClosedPeriodEntity> findByCompanyIdOrderByPeriodDesc(UUID companyId);

    boolean existsByCompanyIdAndPeriod(UUID companyId, String period);
}
