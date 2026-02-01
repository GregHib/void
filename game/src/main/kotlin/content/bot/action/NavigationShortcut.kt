package content.bot.action

import content.bot.fact.Condition

data class NavigationShortcut(
    override val id: String,
    val weight: Int,
    override val requires: List<Condition> = emptyList(),
    override val resolve: List<Condition> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<Condition> = emptySet(),
) : Behaviour