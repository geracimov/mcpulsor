package ru.geracimov.ai.mcpulsor.host

import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class McpulsorHostApplication

fun main(args: Array<String>) {
    val host = runApplication<McpulsorHostApplication>(*args).getBean<Host>()

    val firstQuestion = "какой у меня пульс был за последние 87дней?"
    val secondQuestion = "как дела?"
    val thirdQuestion = "какой у меня был пульс за последние 15 дней если к нему прибавить 1000?"
    sequenceOf(firstQuestion, secondQuestion, thirdQuestion).forEach {
        host.printAnswerToUser(it)
    }

}
