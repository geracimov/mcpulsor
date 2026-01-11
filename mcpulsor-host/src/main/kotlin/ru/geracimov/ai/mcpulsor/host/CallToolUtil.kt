package ru.geracimov.ai.mcpulsor.host

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.spec.McpSchema
import java.util.regex.Pattern

object CallToolUtil {

    private val mapper = ObjectMapper()

    private val TOOL_CALL_PATTERN: Pattern = Pattern.compile(
        "<tool_call>\\s*(\\{.*?})\\s*</tool_call>",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    fun isToolRequired(modelAnswer: String): Boolean = TOOL_CALL_PATTERN.matcher(modelAnswer).find()

    fun getRequiredTool(callToolRequest: String): McpSchema.CallToolRequest {
        val matcher = TOOL_CALL_PATTERN.matcher(callToolRequest)
        matcher.find()
        val callToolRequestJson = matcher.group(1).trim()
        val toolName = mapper.readTree(callToolRequestJson).get("name").asText()
        return McpSchema.CallToolRequest.builder().name(toolName).build()
    }

    fun wrapResponse(response: String) = String.format(
        "<tool_response>%s</tool_response>",
        response
    )

}