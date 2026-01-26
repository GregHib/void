package content.bot.req

import content.bot.Bot

sealed interface Requirement {
    fun satisfied(bot: Bot): Boolean = false
}

data class CloneRequirement(
    val id: String,
) : Requirement