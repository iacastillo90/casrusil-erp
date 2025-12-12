package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.event.EntryCorrectedEvent;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.ClassificationRule;
import com.casrusil.siierpai.modules.accounting.domain.port.out.ClassificationRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de aprendizaje automático que implementa el Feedback Loop (Phase
 * 33).
 * 
 * <p>
 * Captura las correcciones manuales del usuario sobre asientos contables
 * generados
 * automáticamente y las convierte en reglas de clasificación reutilizables.
 * Este es el corazón del sistema de aprendizaje - transforma errores en
 * conocimiento.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Escuchar eventos {@link EntryCorrectedEvent}</li>
 * <li>Extraer patrones de las correcciones (ej: "JUMBO" → cuenta 510101)</li>
 * <li>Crear {@link ClassificationRule} con confidence scoring</li>
 * <li>Aplicar reglas aprendidas en futuras clasificaciones</li>
 * </ul>
 * 
 * <h2>Flujo de aprendizaje:</h2>
 * <ol>
 * <li>Usuario corrige un asiento contable</li>
 * <li>Se publica {@link EntryCorrectedEvent}</li>
 * <li>Este servicio extrae el patrón (ej: descripción de factura)</li>
 * <li>Crea regla: "Si descripción contiene X, usar cuenta Y"</li>
 * <li>Regla se aplica automáticamente en el futuro</li>
 * <li>Confidence aumenta con cada aplicación exitosa</li>
 * </ol>
 * 
 * <h2>Ejemplo:</h2>
 * 
 * <pre>{@code
 * // Usuario corrige: "Compra JUMBO" de cuenta 999999 a 510101
 * // Sistema aprende:
 * ClassificationRule rule = new ClassificationRule(
 *     pattern: "JUMBO",
 *     accountCode: "510101",
 *     confidence: 0.7
 * );
 * // Próxima factura con "JUMBO" se clasifica automáticamente
 * }</pre>
 * 
 * @see EntryCorrectedEvent
 * @see ClassificationRule
 * @see com.casrusil.siierpai.modules.accounting.application.listener.InvoiceAccountingListener
 * @since 1.0
 */
@Service
public class LearningService {

    private static final Logger logger = LoggerFactory.getLogger(LearningService.class);
    private final ClassificationRuleRepository ruleRepository;

    public LearningService(ClassificationRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Escucha eventos de corrección y aprende de ellos.
     */
    @EventListener
    public void onEntryCorrected(EntryCorrectedEvent event) {
        logger.info("🧠 Learning from correction: {}", event.extractMainChange());

        // Extraer patrones de la corrección
        String pattern = extractPattern(event);
        String newAccountCode = extractNewAccountCode(event);

        if (pattern == null || newAccountCode == null) {
            logger.warn("⚠️ Could not extract pattern from correction, skipping learning");
            return;
        }

        // Verificar si ya existe una regla similar
        List<ClassificationRule> existingRules = ruleRepository.findByCompanyIdAndPattern(
                event.getOriginalEntry().getCompanyId(),
                pattern);

        if (!existingRules.isEmpty()) {
            // Actualizar regla existente
            ClassificationRule existingRule = existingRules.get(0);
            if (existingRule.getAccountCode().equals(newAccountCode)) {
                // La corrección confirma la regla existente
                existingRule.recordApplication(true);
                ruleRepository.save(existingRule);
                logger.info("✅ Confirmed existing rule: {} -> {}", pattern, newAccountCode);
            } else {
                // La corrección contradice la regla existente
                existingRule.recordApplication(false);
                ruleRepository.save(existingRule);

                // Crear nueva regla con mayor confianza
                createNewRule(event, pattern, newAccountCode);
            }
        } else {
            // Crear nueva regla
            createNewRule(event, pattern, newAccountCode);
        }
    }

    /**
     * Crea una nueva regla de clasificación basada en la corrección.
     */
    private void createNewRule(EntryCorrectedEvent event, String pattern, String accountCode) {
        String learnedFrom = String.format(
                "Corrección manual: %s | Razón: %s",
                event.extractMainChange(),
                event.getCorrectionReason() != null ? event.getCorrectionReason() : "No especificada");

        ClassificationRule newRule = ClassificationRule.create(
                event.getOriginalEntry().getCompanyId(),
                pattern,
                accountCode,
                learnedFrom);

        ruleRepository.save(newRule);
        logger.info("🎓 Learned new rule: {} -> {}", pattern, accountCode);
    }

    /**
     * Extrae el patrón de la descripción del asiento original.
     * Por ejemplo, si la descripción es "Compra en JUMBO", el patrón sería "JUMBO".
     */
    private String extractPattern(EntryCorrectedEvent event) {
        String description = event.getOriginalEntry().getDescription();
        if (description == null || description.isEmpty()) {
            return null;
        }

        // Estrategia simple: usar palabras clave
        // En una implementación más sofisticada, podrías usar NLP
        String[] words = description.split("\\s+");

        // Buscar palabras en mayúsculas (probablemente nombres de proveedores)
        for (String word : words) {
            if (word.length() > 3 && word.equals(word.toUpperCase())) {
                return word;
            }
        }

        // Si no hay palabras en mayúsculas, usar la descripción completa
        return description.trim();
    }

    /**
     * Extrae el nuevo código de cuenta de la corrección.
     */
    private String extractNewAccountCode(EntryCorrectedEvent event) {
        // Comparar líneas y encontrar la que cambió
        for (int i = 0; i < Math.min(
                event.getOriginalEntry().getLines().size(),
                event.getCorrectedEntry().getLines().size()); i++) {
            AccountingEntryLine originalLine = event.getOriginalEntry().getLines().get(i);
            AccountingEntryLine correctedLine = event.getCorrectedEntry().getLines().get(i);

            if (!originalLine.accountCode().equals(correctedLine.accountCode())) {
                return correctedLine.accountCode();
            }
        }

        return null;
    }

    /**
     * Busca reglas aplicables para una descripción dada.
     */
    public List<ClassificationRule> findApplicableRules(
            com.casrusil.siierpai.shared.domain.valueobject.CompanyId companyId,
            String description) {
        List<ClassificationRule> allRules = ruleRepository.findByCompanyId(companyId);

        return allRules.stream()
                .filter(rule -> rule.matches(description))
                .filter(ClassificationRule::isHighConfidence)
                .toList();
    }
}
