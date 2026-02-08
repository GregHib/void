package content.bot.behaviour.navigation

import content.bot.behaviour.Behaviour
import content.bot.action.BotAction
import content.bot.req.Requirement

data class NavigationShortcut(
    override val id: String,
    val weight: Int,
    override val requires: List<Requirement<*>> = emptyList(),
    override val setup: List<Requirement<*>> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<Requirement<*>> = emptySet(),
) : Behaviour