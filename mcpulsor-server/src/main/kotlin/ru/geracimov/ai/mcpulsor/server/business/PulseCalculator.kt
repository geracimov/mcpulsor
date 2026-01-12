package ru.geracimov.ai.mcpulsor.server.business

import java.util.*

object PulseCalculator {
    val RANDOM: Random = Random()

    fun getPulse(name: String): Int {
        return name.hashCode() % 100 + RANDOM.nextInt(100)
    }
}
