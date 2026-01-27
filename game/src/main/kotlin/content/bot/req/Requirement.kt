package content.bot.req

import content.bot.Bot

/**
 * A requirement that must be satisfied for a bot to perform a behaviour.
 * @param priority Ensure bots aren't walking to locations before getting items etc... Lower values are prioritised first.
 */
sealed class Requirement(val priority: Int) {
    fun satisfied(bot: Bot): Boolean = false
}

internal data class CloneRequirement(
    val id: String,
) : Requirement(-1)

internal data class RequiresReference(
    var requirement: Requirement,
    val references: Map<String, String>,
) : Requirement(-1)