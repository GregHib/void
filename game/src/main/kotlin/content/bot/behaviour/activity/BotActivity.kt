package content.bot.behaviour.activity

import content.bot.behaviour.action.BotAction
import content.bot.behaviour.Behaviour
import content.bot.behaviour.Condition

/**
 * An activity with a limited number of slots that bots can perform
 * E.g. cutting oak trees in varrock, mining copper ore in lumbridge
 */
data class BotActivity(
    override val id: String,
    val capacity: Int,
    override val timeout: Int = 50,
    override val requires: List<Condition> = emptyList(),
    override val setup: List<Condition> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<String> = emptySet(),
) : Behaviour
