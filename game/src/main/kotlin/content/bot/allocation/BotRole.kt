package content.bot.allocation

import content.bot.weightedRandom

/**
 * A fixed job a bot is given by a [WeightedAllocator], doing [activity] for its whole life.
 * [weight], [min] and [max] decide what share of an allocation's bots get this role.
 */
data class BotRole(
    val id: String,
    val activity: String,
    val weight: Int = 1,
    val min: Int = 0,
    val max: Int? = null,
    val builds: List<Pair<Build, Int>> = emptyList(),
    val combatStyle: String? = null,
) {
    fun pickBuild(): Build? = builds.weightedRandom()
}
