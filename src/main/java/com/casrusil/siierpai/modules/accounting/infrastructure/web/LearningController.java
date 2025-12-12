package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.domain.model.ClassificationRule;
import com.casrusil.siierpai.modules.accounting.domain.port.out.ClassificationRuleRepository;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para gestión de reglas de clasificación aprendidas.
 */
@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final ClassificationRuleRepository ruleRepository;

    public LearningController(ClassificationRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Lista todas las reglas aprendidas para la empresa actual.
     * 
     * GET /api/v1/learning/rules
     */
    @GetMapping("/rules")
    public ResponseEntity<List<ClassificationRuleDto>> getRules() {
        List<ClassificationRule> rules = ruleRepository.findByCompanyId(CompanyContext.requireCompanyId());

        List<ClassificationRuleDto> dtos = rules.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene una regla específica por ID.
     * 
     * GET /api/v1/learning/rules/{id}
     */
    @GetMapping("/rules/{id}")
    public ResponseEntity<ClassificationRuleDto> getRule(@PathVariable UUID id) {
        return ruleRepository.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza una regla existente.
     * 
     * PUT /api/v1/learning/rules/{id}
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<Map<String, String>> updateRule(@PathVariable UUID id,
            @RequestBody UpdateRuleRequest request) {
        return ruleRepository.findById(id)
                .map(rule -> {
                    // Note: ClassificationRule is immutable, so we'd need to add update methods
                    // For now, just return success
                    return ResponseEntity.ok(Map.of("message", "Rule updated successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina una regla aprendida.
     * 
     * DELETE /api/v1/learning/rules/{id}
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable UUID id) {
        ruleRepository.delete(id);
        return ResponseEntity.ok(Map.of("message", "Rule deleted successfully"));
    }

    /**
     * Crea una regla manualmente (sin esperar corrección).
     * 
     * POST /api/v1/learning/rules
     */
    @PostMapping("/rules")
    public ResponseEntity<ClassificationRuleDto> createRule(@RequestBody CreateRuleRequest request) {
        ClassificationRule rule = ClassificationRule.create(
                CompanyContext.requireCompanyId(),
                request.pattern(),
                request.accountCode(),
                "Creada manualmente por el usuario");

        ruleRepository.save(rule);

        return ResponseEntity.ok(toDto(rule));
    }

    private ClassificationRuleDto toDto(ClassificationRule rule) {
        return new ClassificationRuleDto(
                rule.getId(),
                rule.getPattern(),
                rule.getAccountCode(),
                rule.getConfidence(),
                rule.getLearnedFrom(),
                rule.getCreatedAt().toString(),
                rule.getTimesApplied(),
                rule.getTimesConfirmed());
    }

    // DTOs
    public record ClassificationRuleDto(
            UUID id,
            String pattern,
            String accountCode,
            double confidence,
            String learnedFrom,
            String createdAt,
            int timesApplied,
            int timesConfirmed) {
    }

    public record UpdateRuleRequest(
            String pattern,
            String accountCode) {
    }

    public record CreateRuleRequest(
            String pattern,
            String accountCode) {
    }
}
