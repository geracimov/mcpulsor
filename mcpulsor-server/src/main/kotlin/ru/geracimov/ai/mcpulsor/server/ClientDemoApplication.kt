package ru.geracimov.ai.mcpulsor.server

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import io.modelcontextprotocol.spec.McpSchema

fun main() {

    val clientTransport = HttpClientStreamableHttpTransport
        .builder("http://localhost:8091")
        .endpoint("/mcpulsor")
        .build()

    with(McpClient.sync(clientTransport).build()) {
        initialize()
        println("Tool list:")
        listTools().tools.forEach { println(it) }
        println("------------------")

        callTool(
            McpSchema.CallToolRequest.builder()
                .name("bioSensor")
                .arguments(mapOf("days" to 666))
                .build()
        ).content.forEach { println("Результат: $it") }
    }


}
