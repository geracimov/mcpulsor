package ru.geracimov.ai.mcpulsor.host

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import io.modelcontextprotocol.spec.McpSchema
import jakarta.annotation.PostConstruct
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.stereotype.Service

@Service
class Host(
    private val chatClient: ChatClient,
    private val chatModel: ChatModel,
) {

    private lateinit var systemPrompt: String
    private lateinit var client: McpSyncClient

    @PostConstruct
    fun init() {
        println("Initialization...")
        val transport = HttpClientStreamableHttpTransport.builder("http://localhost:8091")
            .endpoint("/mcpulsor")
            .build()
        client = McpClient.sync(transport)
            .sampling { messageRequest ->
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

                return@sampling McpSchema.CreateMessageResult.builder()
                    .content(McpSchema.TextContent(samplingAnswer))
                    .build()
            }
            .loggingConsumer {
                println("Клиент: сообщение от сервера ${it.level} ${it.data}")
            }
            .capabilities(McpSchema.ClientCapabilities.builder().sampling().build())
            .build()

        with(client) {
            initialize()
            systemPrompt = SystemPromptFactory.withTools(listTools())
        }
        println("Initialization complete")
    }

    fun printAnswerToUser(question: String) {
        var assistantMessage = chatClient.prompt()
            .system(systemPrompt)
            .user(question)
            .call()
            .chatResponse()!!.result.output

        if (CallToolUtil.isToolRequired(assistantMessage.text!!)) {
            val requiredTool = CallToolUtil.getRequiredTool(assistantMessage.text!!)
            val toolResponse = CallToolUtil.wrapResponse(client.callTool(requiredTool).content.first().toString())

            val toolMessage = UserMessage(toolResponse)
            val userMessage = UserMessage(question)


            assistantMessage = chatClient.prompt()
                .system(systemPrompt)
                .messages(listOf(userMessage, assistantMessage, toolMessage))
                .call()
                .chatResponse()!!.result.output
        }

        println(assistantMessage.text)
    }


}