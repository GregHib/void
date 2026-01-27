package content.bot.req

import content.bot.Bot

sealed interface Requirement {
    fun satisfied(bot: Bot): Boolean = false
}

internal data class CloneRequirement(
    val id: String,
) : Requirement

internal data class RequiresReference(
    var requirement: Requirement,
    val references: Map<String, String>,
) : Requirement