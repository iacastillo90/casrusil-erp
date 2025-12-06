package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.ClassificationRule;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.ClassificationRuleRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador JPA para ClassificationRuleRepository.
 */
/**
 * Adaptador de persistencia para reglas de clasificación.
 * 
 * <p>
 * Implementa {@link ClassificationRuleRepository} para gestionar el
 * almacenamiento
 * de reglas de clasificación bancaria.
 * 
 * @since 1.0
 */
@Component
public class ClassificationRuleJpaAdapter implements ClassificationRuleRepository {

    private final ClassificationRuleJpaRepository jpaRepository;

    public ClassificationRuleJpaAdapter(ClassificationRuleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(ClassificationRule rule) {
        ClassificationRuleEntity entity = toEntity(rule);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<ClassificationRule> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ClassificationRule> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassificationRule> findByCompanyIdAndPattern(CompanyId companyId, String pattern) {
        return jpaRepository.findByCompanyIdAndPattern(companyId.value(), pattern)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    private ClassificationRuleEntity toEntity(ClassificationRule rule) {
        return new ClassificationRuleEntity(
                rule.getId(),
                rule.getCompanyId().value(),
                rule.getPattern(),
                rule.getAccountCode(),
                rule.getConfidence(),
                rule.getLearnedFrom(),
                rule.getCreatedAt(),
                rule.getTimesApplied(),
                rule.getTimesConfirmed());
    }

    private ClassificationRule toDomain(ClassificationRuleEntity entity) {
        ClassificationRule rule = new ClassificationRule(
                entity.getId(),
                new CompanyId(entity.getCompanyId()),
                entity.getPattern(),
                entity.getAccountCode(),
                entity.getConfidence(),
                entity.getLearnedFrom());

        // Restore usage statistics
        for (int i = 0; i < entity.getTimesApplied(); i++) {
            rule.recordApplication(i < entity.getTimesConfirmed());
        }

        return rule;
    }
}
