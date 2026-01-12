package ru.geracimov.ai.mcpulsor.server.tools

import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpTool
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springaicommunity.mcp.context.McpSyncRequestContext
import org.springframework.stereotype.Service
import ru.geracimov.ai.mcpulsor.server.business.MedicalProfileProvider
import ru.geracimov.ai.mcpulsor.server.business.PulseCalculator

@Service
@Suppress("unused")
class MCPulsorTools {

    private val samplingSystemPrompt = """
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


    @McpTool(
        name = "diagnostator",
        title = "Diagnostic Tool by user name",
        description = "Used to obtain a diagnosis by username. Always returns either the name of the disease or a message that the person is not sick with anything",
    )
    fun callDiagnostator(
        mcpSyncServerExchange: McpSyncRequestContext,
        @McpToolParam(
            required = true,
            description = "Name of the patient from whom you want to determine the current diagnosis",
        )
        name: String,
    ): String {
        val pulse: Int = PulseCalculator.getPulse(name)
        val medicalProfile: String = MedicalProfileProvider.getMedicalProfile(name)
        val samplingPrompt =
            "вот такой у нас пациент, вот его медицинская карта '$medicalProfile' а вот его текущий пульс: $pulse"

        val samplingMessageRequest = McpSchema.CreateMessageRequest.builder()
            .systemPrompt(samplingSystemPrompt)
            .temperature(0.8)
            .maxTokens(200)
            .messages(listOf(McpSchema.SamplingMessage(McpSchema.Role.USER, McpSchema.TextContent(samplingPrompt))))
            .build()

        val samplingResult = mcpSyncServerExchange.sample(samplingMessageRequest)

        return samplingResult.content().toString()
    }


    @McpTool(
        name = "bioSensor",
        //title = "Human Vital Pulse Sensor",
        description = "Returns the current heart rate of the user as a simple string value",
    )
    fun callBioSensor(numberOfDays: Int): Map<String, Any> = calculateResult(numberOfDays)

    private fun calculateResult(days: Int): Map<String, Any> = mapOf<String, Any>(
        "pulse" to "Пульс пользователя за $days дней был ${if (days > 30) 66 else 33} ударов в минуту",
        "state" to "Тебе кабзда",
        "sleepDeprivation" to true,
    )

}
