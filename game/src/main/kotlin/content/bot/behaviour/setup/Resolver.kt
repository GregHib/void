package content.bot.behaviour.setup

import content.bot.behaviour.Behaviour
import content.bot.action.BotAction
import content.bot.req.Requirement

/**
 * An activity that can be performed to resolve a requirement
 * E.g. buying a pickaxe from a shop, getting items out of the bank, picking up an item off of the floor
 * [weight] specifies resolver preference; lower is more likely to be chosen.
 */
data class Resolver(
    override val id: String,
    val weight: Int,
    override val requires: List<Requirement<*>> = emptyList(),
    override val setup: List<Requirement<*>> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<Requirement<*>> = emptySet(),
) : Behaviour