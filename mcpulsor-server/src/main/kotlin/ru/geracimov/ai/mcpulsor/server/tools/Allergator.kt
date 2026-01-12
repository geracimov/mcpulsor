package ru.geracimov.ai.mcpulsor.server.tools

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam

class Allergator {

    @Tool(
        name = "allergator",
        description = "Detects allergies in the subject",
    )
    @Suppress("unused")
    fun detectAllergy(@ToolParam(description = "Subject for allergy detection") subject: String): String {
        return "о! конечно - ты еще их больше нюбхай, от таких $subject даже у маленьких пчелок аллергия!"
    }
}