package ru.geracimov.ai.mcpulsor.host

import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpLogging
import org.springaicommunity.mcp.annotation.McpSampling
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.stereotype.Service

@Service
class Host(
    private val chatClient: ChatClient,
    private val chatModel: ChatModel,
) {
    //client from application.yaml
    @McpLogging(clients = ["mcpulsor-server"])
    @Suppress("unused")
    fun logClient(it: McpSchema.LoggingMessageNotification) {
        println("Клиент: сообщение от сервера ${it.level} ${it.data}")
    }

    //client from application.yaml
    @McpSampling(clients = ["mcpulsor-server"])
    @Suppress("unused")
    fun doSampling(messageRequest: McpSchema.CreateMessageRequest): McpSchema.CreateMessageResult {
        val samplingChatClient = ChatClient.builder(chatModel)
            .defaultOptions(
                OllamaChatOptions.builder()
                    .temperature(messageRequest.temperature())
                    .numPredict(messageRequest.maxTokens)
                    .build()
            )
            .build()

        val samplingAnswer = samplingChatClient.prompt().system(messageRequest.systemPrompt)
            .user(messageRequest.messages.joinToString { it.content().toString() })
            .call()
            .content()

        return McpSchema.CreateMessageResult.builder()
            .content(McpSchema.TextContent(samplingAnswer))
            .build()
    }

    fun printAnswerToUser(question: String) {
        val assistantMessage = chatClient.prompt()
            .user(question)
            .call()
            .chatResponse()!!.result.output

        println(assistantMessage.text)
    }


}