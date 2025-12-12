package com.casrusil.siierpai.modules.ai_assistant.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.ai_assistant.application.service.ConversationService;
import com.casrusil.siierpai.modules.ai_assistant.domain.model.Conversation;
import com.casrusil.siierpai.modules.ai_assistant.domain.model.Message;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para el Asistente de IA Financiero.
 * 
 * <p>
 * Gestiona las conversaciones entre el usuario y el modelo de IA (Gemini).
 * Permite iniciar nuevas sesiones y enviar mensajes, manteniendo el contexto
 * de la empresa actual.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/ai/conversations}: Iniciar nueva conversación.</li>
 * <li>{@code POST /api/v1/ai/conversations/{id}/messages}: Enviar mensaje al
 * asistente.</li>
 * </ul>
 * 
 * @see ConversationService
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiAssistantController {

    private final ConversationService conversationService;

    public AiAssistantController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> startConversation() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        // In a real app, we'd get UserId from SecurityContext. For now, using a
        // placeholder or random.
        UserId userId = new UserId(UUID.randomUUID());
        return ResponseEntity.ok(conversationService.startConversation(companyId, userId));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(conversationService.sendMessage(conversationId, request.content()));
    }

    public record SendMessageRequest(String content) {
    }
}
