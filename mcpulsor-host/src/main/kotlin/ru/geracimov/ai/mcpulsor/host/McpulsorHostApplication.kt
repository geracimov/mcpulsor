package ru.geracimov.ai.mcpulsor.host

import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class McpulsorHostApplication

fun main(args: Array<String>) {
    val host = runApplication<McpulsorHostApplication>(*args).getBean<Host>()

/*    val firstQuestion = "какой у меня пульс был за последние 7 дней? Я буду жить?"
    val secondQuestion = "как я себя чувствую?"
    val thirdQuestion = "учитывая мое здоровье за последние 5 дней, бужать ли мне марафоны?"
    sequenceOf(firstQuestion, secondQuestion, thirdQuestion).forEach {
        host.printAnswerToUser(it)
    }*/

    host.printAnswerToUser("Я Лена, скажи, чем я болен?")
    host.printAnswerToUser("Я Вася, скажи, чем я болен?")
    host.printAnswerToUser("Я Коля, скажи, чем я болен?")

}
