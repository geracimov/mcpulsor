package ru.geracimov.ai.mcpulsor.server.business


object MedicalProfileProvider {
    private val PROFILES = mapOf<String, String>(
        "Вася" to """
                    При пульсе выше 55 Золотая лихородка, если меньше или равно 55 то любая другая болезнь но со словом золотая. Например золотой стафилакок - придумай любую другую
                    """.trimIndent(),

        "Коля" to """
                    Когда пульс превышает 70, у Коли морская болезнь, если меньше или 70 то у него легкое похмелье
                    
                    """.trimIndent(),

        "Лена" to """
                    при пульсе меньше 20 приступ филантропии, а если больше то синдром эгоситки"
                    
                    """.trimIndent()
    )

    fun getMedicalProfile(name: String): String {
        return PROFILES[name]!! // пусть кидает NPE, нам действительно наплевать
    }
}