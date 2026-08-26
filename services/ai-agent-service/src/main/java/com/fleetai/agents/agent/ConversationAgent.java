package com.fleetai.agents.agent;

import com.fleetai.agents.dto.AgentRequest;
import com.fleetai.agents.dto.AgentResponse;
import com.fleetai.agents.rag.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG-grounded chat interface for supervisors (surfaced in the dashboard's chat
 * panel). Retrieves relevant chunks from the pgvector store — driver history,
 * incident reports, compliance/policy documents — before answering, so responses
 * are grounded in the client's actual data rather than the model's general
 * knowledge, and every answer carries citations back to its source documents.
 */
@Component
public class ConversationAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ConversationAgent.class);

    private static final String SYSTEM_PROMPT = """
        You are the fleet operations assistant for supervisors. Answer questions
        about drivers, routes, alerts, and policy using ONLY the provided context
        documents. If the context doesn't contain the answer, say so plainly
        instead of guessing. Keep answers concise and reference which document
        each fact came from.
        """;

    private final ChatClient chatClient;
    private final RagService ragService;

    public ConversationAgent(ChatClient.Builder builder, RagService ragService) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
        this.ragService = ragService;
    }

    @Override
    public String name() {
        return "conversation-agent";
    }

    @Override
    public AgentResponse execute(AgentRequest request) {
        log.info("CANARY: execute() entered, query={}", request.getQuery());

        List<Document> retrieved = ragService.retrieve(request.getQuery(), 5);
        log.info("CANARY: retrieve() returned {} documents", retrieved.size());

        String groundingContext = retrieved.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
        log.info("CANARY: about to call chatClient, context length={}", groundingContext.length());

        String answer;
        try {
            answer = chatClient.prompt()
                    .user(u -> u.text("""
                        Context documents:
                        {context}

                        Supervisor question: {question}
                        """)
                        .param("context", groundingContext.isBlank() ? "(no matching documents found)" : groundingContext)
                        .param("question", request.getQuery()))
                    .call()
                    .content();
            log.info("CANARY: chatClient call returned successfully, answer length={}", answer == null ? -1 : answer.length());
        } catch (Exception e) {
            log.error("CANARY: chatClient call threw exception", e);
            throw e;
        }

        List<String> citations = retrieved.stream()
                .map(d -> String.valueOf(d.getMetadata().getOrDefault("source", d.getId())))
                .distinct()
                .toList();

        return AgentResponse.builder()
                .agentName(name())
                .summary(answer)
                .severity("INFO")
                .requiresApproval(false)
                .citations(citations)
                .data(Map.of("retrievedChunks", retrieved.size()))
                .build();
    }
}
