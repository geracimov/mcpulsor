package ru.geracimov.ai.mcpulsor.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import io.modelcontextprotocol.spec.McpSchema
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.servlet.ServletHolder
import org.eclipse.jetty.server.Server
import io.github.oshai.kotlinlogging.KotlinLogging
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper
import io.modelcontextprotocol.server.McpServerFeatures


val log = KotlinLogging.logger { }

fun main() {
    log.info { "Server Application starting..." }

    val transportProvider = HttpServletStreamableServerTransportProvider.builder().mcpEndpoint("/mcpulsor").build()
    McpServer.sync(transportProvider)
        .serverInfo("mcpulsor MCP server", "1.0.0-RELEASE")
        .capabilities(createServerCapabilities())
        .tools(*createToolSpecifications())
        .build()

    val servletContextHandler = ServletContextHandler(ServletContextHandler.SESSIONS).apply {
        contextPath = "/"
        addServlet(ServletHolder(transportProvider), "/*")
    }

    Server(8091).apply {
        handler = servletContextHandler
        start()
        log.info { "Server Application started" }
        join()
    }

}

fun createToolSpecifications(): Array<McpServerFeatures.SyncToolSpecification> {
    val bioSensorTool = McpSchema.Tool.builder()
        .name("bioSensor")
        .title("Human Vital Pulse Sensor")
        .description("Returns the current heart rate of the user as a simple string value")
        .inputSchema(JacksonMcpJsonMapper(ObjectMapper()), createBioSensorInputSchema())
        .outputSchema(JacksonMcpJsonMapper(ObjectMapper()), createBioSensorOutputSchema())
        .build()

    val bioSensorToolSpecification = McpServerFeatures.SyncToolSpecification.builder()
        .tool(bioSensorTool)
        .callHandler { _, callToolRequest ->
            val days = callToolRequest.arguments["days"].toString().toInt()
            calculateResult(days)
        }
        .build()

    return arrayOf(bioSensorToolSpecification)
}

private fun calculateResult(days: Int): McpSchema.CallToolResult? {
    val properties = mapOf<String, Any>(
        "pulse" to "Пульс пользователя за $days дней был ${if (days > 30) 66 else 33} ударов в минуту",
        "state" to "Тебе кабзда",
        "sleepDeprivation" to true,
    )

    return McpSchema.CallToolResult.builder()
        .structuredContent(properties)
        .isError(false)
        .build()
}

fun createBioSensorOutputSchema(): String {
    val rootJsonNode = ObjectMapper().createObjectNode()
        .put("type", "object")
    val propertiesJsonNode = rootJsonNode.putObject("properties")
    propertiesJsonNode.putObject("pulse")
        .put("type", "string")
        .put("description", "The current pulse rate of the user for last days")
        .put("minLength", "1")
    propertiesJsonNode.putObject("state")
        .put("type", "string")
        .put("description", "The current state of the user")
    propertiesJsonNode.putObject("sleepDeprivation")
        .put("type", "boolean")
        .put("description", "Whether the user is sleep deprived or not")
    return rootJsonNode.toString()
}

fun createBioSensorInputSchema(): String {
    val root = ObjectMapper().createObjectNode()
        .put("type", "object")
    root.putObject("properties")
        .putObject("days")
        .put("type", "integer")
        .put("description", "Number of past days to include in the pulse reading request")
    root.putArray("required")
        .add("days")
    return root.toString()
}

fun createServerCapabilities(): McpSchema.ServerCapabilities = McpSchema.ServerCapabilities.builder()
    .tools(true)
    .build()
