package content.bot.action

import content.bot.fact.Requirement

data class NavigationShortcut(
    override val id: String,
    val weight: Int,
    override val requires: List<Requirement<*>> = emptyList(),
    override val setup: List<Requirement<*>> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<Requirement<*>> = emptySet(),
) : Behaviour