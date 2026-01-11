package ru.geracimov.ai.mcpulsor.host

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import jakarta.annotation.PostConstruct
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.stereotype.Service

@Service
class Host(private val chatClient: ChatClient) {

    private lateinit var systemPrompt: String
    private lateinit var client: McpSyncClient

    @PostConstruct
    fun init() {
        println("Initialization...")
        val transport = HttpClientStreamableHttpTransport.builder("http://localhost:8091")
            .endpoint("/mcpulsor")
            .build()
        client = McpClient.sync(transport).build()

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