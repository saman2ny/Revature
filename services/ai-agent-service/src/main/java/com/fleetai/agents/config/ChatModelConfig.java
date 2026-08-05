package com.fleetai.agents.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes a prototype ChatClient.Builder that each agent customizes with its own
 * system prompt (see agent/*.java). The underlying ChatModel bean (Anthropic) is
 * auto-configured by spring-ai-anthropic-spring-boot-starter from the
 * ANTHROPIC_API_KEY / LLM_MODEL env vars in application.yml — nothing to wire here
 * beyond exposing the builder.
 *
 * To add a fallback/secondary provider, add its starter, mark this bean @Primary,
 * and inject the alternate ChatModel directly where needed.
 */
@Configuration
public class ChatModelConfig {

    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
