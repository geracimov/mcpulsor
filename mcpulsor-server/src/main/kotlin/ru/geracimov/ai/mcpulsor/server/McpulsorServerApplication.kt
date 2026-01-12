package ru.geracimov.ai.mcpulsor.server

import org.springframework.ai.support.ToolCallbacks
import org.springframework.ai.tool.ToolCallback
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import ru.geracimov.ai.mcpulsor.server.tools.Allergator


@SpringBootApplication
class McpulsorServerApplication{

    @Bean
    fun toolCallbacks(): List<ToolCallback> = ToolCallbacks.from(Allergator()).toList()

}

fun main(args: Array<String>) {
    runApplication<McpulsorServerApplication>(*args)
}
