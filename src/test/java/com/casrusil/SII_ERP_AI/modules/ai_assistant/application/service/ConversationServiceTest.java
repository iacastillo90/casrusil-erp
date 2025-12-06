package com.casrusil.SII_ERP_AI.modules.ai_assistant.application.service;

import com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model.Conversation;
import com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model.Message;
import com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model.Tool;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.UserId;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class ConversationServiceTest {

    private ConversationService conversationService;

    @Mock
    private ChatLanguageModel chatLanguageModel;

    @Mock
    private Tool mockTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockTool.name()).thenReturn("mockTool");
        when(mockTool.description()).thenReturn("A mock tool");

        conversationService = new ConversationService(chatLanguageModel, List.of(mockTool));
    }

    @Test
    void startConversation_ShouldCreateNewConversation() {
        CompanyId companyId = CompanyId.random();
        UserId userId = UserId.random();

        Conversation conversation = conversationService.startConversation(companyId, userId);

        assertNotNull(conversation);
        assertEquals(companyId, conversation.getCompanyId());
        assertEquals(userId, conversation.getUserId());
        assertNotNull(conversation.getId());
    }

    @Test
    void sendMessage_ShouldAddUserAndAiMessages() {
        // Given
        CompanyId companyId = CompanyId.random();
        UserId userId = UserId.random();
        Conversation conversation = conversationService.startConversation(companyId, userId);
        String userContent = "Hello AI";
        String aiContent = "Hello User";

        when(chatLanguageModel.generate(anyList())).thenReturn(Response.from(new AiMessage(aiContent)));

        // When
        Message responseMsg = conversationService.sendMessage(conversation.getId(), userContent);

        // Then
        assertNotNull(responseMsg);
        assertEquals("assistant", responseMsg.role());
        assertEquals(aiContent, responseMsg.content());

        List<Message> messages = conversation.getMessages();
        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).role());
        assertEquals(userContent, messages.get(0).content());
        assertEquals("assistant", messages.get(1).role());
        assertEquals(aiContent, messages.get(1).content());
    }
}
