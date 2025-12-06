package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para ClassificationRuleEntity.
 */
/**
 * Repositorio JPA para reglas de clasificación.
 * 
 * <p>
 * Permite gestionar las reglas utilizadas para la clasificación automática
 * de movimientos bancarios.
 * 
 * @since 1.0
 */
@Repository
public interface ClassificationRuleJpaRepository extends JpaRepository<ClassificationRuleEntity, UUID> {
    List<ClassificationRuleEntity> findByCompanyId(UUID companyId);

    List<ClassificationRuleEntity> findByCompanyIdAndPattern(UUID companyId, String pattern);
}
