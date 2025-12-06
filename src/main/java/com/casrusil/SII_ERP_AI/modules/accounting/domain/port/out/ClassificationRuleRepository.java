package com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.ClassificationRule;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para persistencia de reglas de clasificación contable.
 * 
 * <p>
 * Almacena las reglas aprendidas por el sistema de feedback loop (Phase 33).
 * Estas reglas permiten que el sistema aprenda de las correcciones del usuario
 * y aplique automáticamente la clasificación correcta en el futuro.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Persistir reglas de clasificación aprendidas</li>
 * <li>Buscar reglas por patrón de descripción</li>
 * <li>Gestionar confianza de reglas (confidence scoring)</li>
 * </ul>
 * 
 * @see ClassificationRule
 * @see com.casrusil.SII_ERP_AI.modules.accounting.application.service.LearningService
 * @since 1.0
 */
public interface ClassificationRuleRepository {
    void save(ClassificationRule rule);

    Optional<ClassificationRule> findById(UUID id);

    List<ClassificationRule> findByCompanyId(CompanyId companyId);

    List<ClassificationRule> findByCompanyIdAndPattern(CompanyId companyId, String pattern);

    void delete(UUID id);
}
