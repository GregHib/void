package content.bot.allocation

import world.gregs.voidps.type.random

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

/**
 * Weighted random pick, entries with zero or negative weights are never picked unless all are.
 */
internal fun <T> List<Pair<T, Int>>.weightedRandom(): T? {
    if (size <= 1) {
        return firstOrNull()?.first
    }
    val total = sumOf { it.second.coerceAtLeast(0) }
    if (total <= 0) {
        return first().first
    }
    var roll = random.nextInt(total)
    for ((value, weight) in this) {
        roll -= weight.coerceAtLeast(0)
        if (roll < 0) {
            return value
        }
    }
    return last().first
}
