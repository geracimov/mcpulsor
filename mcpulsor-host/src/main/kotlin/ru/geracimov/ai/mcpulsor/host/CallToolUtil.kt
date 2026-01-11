package ru.geracimov.ai.mcpulsor.host

import com.fasterxml.jackson.core.type.TypeReference
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
        val callToolRequestString = matcher.group(1).trim()
        val callToolRequestJsonNode = mapper.readTree(callToolRequestString)
        val toolName = callToolRequestJsonNode.get("name").asText()
        val parametersJsonNode = callToolRequestJsonNode.path("parameters")
        val args: Map<String, Any> = mapper.convertValue(
            parametersJsonNode,
            object : TypeReference<Map<String, Any>>() {}
        )
        return McpSchema.CallToolRequest.builder()
            .name(toolName)
            .arguments(args)
            .build()
    }

    fun wrapResponse(response: String) = String.format(
        "<tool_response>%s</tool_response>",
        response
    )

}