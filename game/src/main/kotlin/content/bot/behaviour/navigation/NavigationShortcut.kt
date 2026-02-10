package content.bot.behaviour.navigation

import content.bot.behaviour.action.BotAction
import content.bot.behaviour.Behaviour
import content.bot.behaviour.Condition

data class NavigationShortcut(
    override val id: String,
    val weight: Int,
    override val timeout: Int,
    override val requires: List<Condition> = emptyList(),
    override val setup: List<Condition> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<String> = emptySet(),
) : Behaviour
