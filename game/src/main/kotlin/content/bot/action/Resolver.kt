package content.bot.action

import content.bot.fact.Fact

/**
 * An activity that can be performed to resolve a requirement
 * E.g. buying a pickaxe from a shop, getting items out of the bank, picking up an item off of the floor
 */
data class Resolver(
    override val id: String,
    override val requires: List<Fact> = emptyList(),
    override val plan: List<BotAction> = emptyList(),
    override val produces: Set<Fact> = emptySet(),
) : Behaviour