package content.bot.fact

import content.bot.Bot

/**
 * A bots state which can be required for, or a product of performing a [content.bot.action.Behaviour]
 * @param priority Ensure bots aren't walking to locations before getting items etc... lower values are prioritised first.
 */
sealed class Fact(val priority: Int) {
    open fun check(bot: Bot): Boolean = false
}

internal data class FactClone(
    val id: String,
) : Fact(-1)

internal data class FactReference(
    var fact: Fact,
    val references: Map<String, String>,
) : Fact(-1)