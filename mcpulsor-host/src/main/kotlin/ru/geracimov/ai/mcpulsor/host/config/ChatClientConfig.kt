package ru.geracimov.ai.mcpulsor.host.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {


    @Bean
    fun chatClient(chatModel: ChatModel): ChatClient {
        val chatOptions = OllamaChatOptions.builder()
            .temperature(0.1)
            .topK(10)
            .topP(0.95)
            .repeatPenalty(1.0)
            .build()
        return ChatClient.builder(chatModel)
            .defaultOptions(chatOptions)
            .build()
    }
}