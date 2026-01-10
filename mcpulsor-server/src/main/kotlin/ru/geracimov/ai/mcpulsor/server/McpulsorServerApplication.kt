package ru.geracimov.ai.mcpulsor.server

import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import io.modelcontextprotocol.spec.McpSchema
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.servlet.ServletHolder
import org.eclipse.jetty.server.Server
import io.github.oshai.kotlinlogging.KotlinLogging


val log = KotlinLogging.logger { }
fun main() {
    log.info { "Server Application starting..." }

    val transportProvider = HttpServletStreamableServerTransportProvider.builder().mcpEndpoint("/mcpulsor").build()
    McpServer.sync(transportProvider)
        .capabilities(createServerCapabilities())
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

fun createServerCapabilities(): McpSchema.ServerCapabilities {
    return McpSchema.ServerCapabilities.builder()
        .build()
}
