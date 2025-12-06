package com.casrusil.SII_ERP_AI.modules.ai_assistant.application.service;

import com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model.Conversation;
import com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model.Message;
import com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model.Tool;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.UserId;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ChatLanguageModel chatLanguageModel;
    private final List<Tool> tools;
    private final Map<UUID, Conversation> conversations = new ConcurrentHashMap<>();

    public ConversationService(ChatLanguageModel chatLanguageModel, List<Tool> tools) {
        this.chatLanguageModel = chatLanguageModel;
        this.tools = tools;
    }

    public Conversation startConversation(CompanyId companyId, UserId userId) {
        Conversation conversation = new Conversation(companyId, userId);
        conversations.put(conversation.getId(), conversation);
        return conversation;
    }

    public Conversation getConversation(UUID conversationId) {
        return conversations.get(conversationId);
    }

    public Message sendMessage(UUID conversationId, String content) {
        Conversation conversation = conversations.get(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        // 1. Add User Message
        Message userMsg = Message.user(content);
        conversation.addMessage(userMsg);

        // 2. Prepare LangChain4j messages
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new SystemMessage(
                "You are an AI Assistant for an ERP system in Chile. You help users with financial tasks like searching invoices, generating reports, and calculating F29 (VAT declarations). Current Company ID: "
                        + conversation.getCompanyId().value()));

        for (Message msg : conversation.getMessages()) {
            if ("user".equals(msg.role())) {
                chatMessages.add(new UserMessage(msg.content()));
            } else if ("assistant".equals(msg.role())) {
                chatMessages.add(new AiMessage(msg.content()));
            }
        }

        // 3. Prepare Tools
        List<ToolSpecification> toolSpecs = tools.stream()
                .map(tool -> ToolSpecification.builder()
                        .name(tool.name())
                        .description(tool.description())
                        .build())
                .collect(Collectors.toList());

        // 4. Tool Execution Loop
        final String[] finalResponseHolder = new String[1];
        try {
            // Run in company context to ensure tools have access to CompanyContext
            CompanyContext.runInCompanyContext(conversation.getCompanyId(), () -> {
                Response<AiMessage> response = chatLanguageModel.generate(chatMessages, toolSpecs);
                AiMessage aiMessage = response.content();

                // Check if AI wants to execute a tool
                if (aiMessage.hasToolExecutionRequests()) {
                    // Execute all requested tools
                    for (var toolRequest : aiMessage.toolExecutionRequests()) {
                        String toolName = toolRequest.name();
                        String toolArgs = toolRequest.arguments();

                        // Find and execute the tool
                        Tool tool = tools.stream()
                                .filter(t -> t.name().equals(toolName))
                                .findFirst()
                                .orElse(null);

                        String toolResult;
                        if (tool != null) {
                            toolResult = tool.execute(toolArgs);
                        } else {
                            toolResult = "Tool not found: " + toolName;
                        }

                        // Add tool result to messages
                        chatMessages.add(new dev.langchain4j.data.message.ToolExecutionResultMessage(
                                toolRequest.id(),
                                toolName,
                                toolResult));
                    }

                    // Get final response after tool execution
                    Response<AiMessage> finalResp = chatLanguageModel.generate(chatMessages, toolSpecs);
                    finalResponseHolder[0] = finalResp.content().text();
                } else {
                    // No tool execution needed, return direct response
                    finalResponseHolder[0] = aiMessage.text();
                }
            });
        } catch (Exception e) {
            finalResponseHolder[0] = "Error processing request: " + e.getMessage();
        }

        String finalResponse = finalResponseHolder[0];

        // 5. Add Assistant Message
        Message aiMsg = Message.assistant(finalResponse);
        conversation.addMessage(aiMsg);

        return aiMsg;
    }
}
