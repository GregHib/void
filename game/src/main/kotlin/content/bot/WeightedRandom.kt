package content.bot

import world.gregs.voidps.type.random

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
