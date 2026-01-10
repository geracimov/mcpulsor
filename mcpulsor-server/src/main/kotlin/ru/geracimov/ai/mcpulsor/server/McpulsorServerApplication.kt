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
        .inputSchema(JacksonMcpJsonMapper(ObjectMapper()), createBioSensorSchema())
        .build()

    val bioSensorToolSpecification = McpServerFeatures.SyncToolSpecification.builder()
        .tool(bioSensorTool)
        .callHandler { _, _ ->
            McpSchema.CallToolResult.builder()
                .addTextContent("Пульс пользователя 60")
                .isError(false)
                .build()
        }
        .build()

    return arrayOf(bioSensorToolSpecification)
}

fun createBioSensorSchema(): String = ObjectMapper().createObjectNode()
    .put("type", "object")
    .toString()

fun createServerCapabilities(): McpSchema.ServerCapabilities = McpSchema.ServerCapabilities.builder()
    .tools(true)
    .build()
