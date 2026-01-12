package ru.geracimov.ai.mcpulsor.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.McpServerFeatures
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import io.modelcontextprotocol.spec.McpSchema.*
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.servlet.ServletHolder
import org.eclipse.jetty.server.Server
import ru.geracimov.ai.mcpulsor.server.business.MedicalProfileProvider.getMedicalProfile
import ru.geracimov.ai.mcpulsor.server.business.PulseCalculator.getPulse


val log = KotlinLogging.logger { }


val SAMPLING_SYSTEM_PROMPT = """
            Ты ставишь диагноз одним словом.
            
            На вход всегда получаешь медицинскую карту человека и его текущий пульс.
            
            Твоя задача — выдать ровно одно:
            название существующей болезни (может быть 1-3 слова, можно редкие или забавно звучащие),
            или
            
            Ответ: -сказать что пациент здоров.
            Правила:
            — Анализируй карту пациента и пульс и выбирай подходящую болезнь.
            — Отвечай только названием болезни или фразой что пациент здоров.
            — Никаких пояснений, никакого текста вокруг.
            """.trimIndent()

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
    val diagnostatorTool = Tool.builder()
        .name("diagnostator")
        .title("Diagnostic Tool by user name")
        .description("Used to obtain a diagnosis by username. Always returns either the name of the disease or a message that the person is not sick with anything")
        .inputSchema(JacksonMcpJsonMapper(ObjectMapper()), createDiagnostatorInputSchema())
        .build()

    val diagnostatorToolSpec = McpServerFeatures.SyncToolSpecification.builder()
        .tool(diagnostatorTool)
        .callHandler { mcpSyncServerExchange, callToolRequest ->
            println("Сервер бормояет себе в консоль: спросил у клиента, может ли он делать sampling, вот его ответ: ${mcpSyncServerExchange.clientCapabilities.sampling()}")
            val name = callToolRequest.arguments()["name"].toString()
            val pulse: Int = getPulse(name)
            val medicalProfile: String = getMedicalProfile(name)
            val samplingPrompt = "вот такой у нас пациент, вот его медицинская карта '$medicalProfile' а вот его текущий пульс: $pulse"

            val samplingMessageRequest = CreateMessageRequest.builder()
                .systemPrompt(SAMPLING_SYSTEM_PROMPT)
                .temperature(0.8)
                .maxTokens(200)
                .messages(listOf(SamplingMessage(Role.USER, TextContent(samplingPrompt))))
                .build()

            val samplingResult = mcpSyncServerExchange.createMessage(samplingMessageRequest)

            mcpSyncServerExchange.loggingNotification(
                LoggingMessageNotification.builder()
                    .data("Я сервер и получил решил спросить при помощи сэмплинг вот это + '$samplingPrompt', а вот что я получил в ответ: ${samplingResult.content()}")
                    .build()
            )

            CallToolResult.builder().addContent(samplingResult.content()).build()
        }
        .build()

    val bioSensorTool = Tool.builder()
        .name("bioSensor")
        .title("Human Vital Pulse Sensor")
        .description("Returns the current heart rate of the user as a simple string value")
        .inputSchema(JacksonMcpJsonMapper(ObjectMapper()), createBioSensorInputSchema())
        .outputSchema(JacksonMcpJsonMapper(ObjectMapper()), createBioSensorOutputSchema())
        .build()

    val bioSensorToolSpecification = McpServerFeatures.SyncToolSpecification.builder()
        .tool(bioSensorTool)
        .callHandler { mcpSyncServerExchange, callToolRequest ->
            mcpSyncServerExchange.loggingNotification(
                LoggingMessageNotification.builder()
                    .data("Сервер говорит: получил запрос на вызов тула: $callToolRequest")
                    .level(LoggingLevel.EMERGENCY)
                    .build()
            )
            val days = callToolRequest.arguments["days"].toString().toInt()
            calculateResult(days)
        }
        .build()

    return arrayOf(bioSensorToolSpecification, diagnostatorToolSpec)
}

fun createDiagnostatorInputSchema(): String {
    val root = ObjectMapper().createObjectNode()
        .put("type", "object")
    root.putObject("properties")
        .putObject("name")
        .put("type", "string")
        .put("description", "Name of the patient from whom you want to determine the current diagnosis")
    root.putArray("required")
        .add("name")
    return root.toString()
}

private fun calculateResult(days: Int): CallToolResult? {
    val properties = mapOf<String, Any>(
        "pulse" to "Пульс пользователя за $days дней был ${if (days > 30) 66 else 33} ударов в минуту",
        "state" to "Тебе кабзда",
        "sleepDeprivation" to true,
    )

    return CallToolResult.builder()
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

fun createServerCapabilities(): ServerCapabilities = ServerCapabilities.builder()
    .tools(true)
    .build()
