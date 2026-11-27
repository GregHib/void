package content.bot.allocation

import content.bot.Bot

/**
 * Bots are given fixed [roles] in proportion to each role's weight, worked out from the number of bots.
 * Role minimums are filled first, the remainder is split by weight (largest remainder) and clamped to each role's maximum.
 */
class WeightedAllocator(config: AllocationConfig, val roles: List<BotRole>) : BotAllocator(config) {

    override fun role(id: String): BotRole? = roles.firstOrNull { it.id == id }

    override fun allocate(members: List<Bot>): BotRole? {
        if (roles.isEmpty()) {
            return null
        }
        val targets = targets(maxOf(count, members.size + 1))
        val current = IntArray(roles.size)
        for (bot in members) {
            val index = roles.indexOfFirst { it.id == bot.role }
            if (index != -1) {
                current[index]++
            }
        }
        var best = 0
        for (index in 1 until roles.size) {
            val deficit = targets[index] - current[index]
            val bestDeficit = targets[best] - current[best]
            if (deficit > bestDeficit || (deficit == bestDeficit && roles[index].weight > roles[best].weight)) {
                best = index
            }
        }
        return roles[best]
    }

    /**
     * Number of bots each role should have when there are [size] bots in total.
     */
    fun targets(size: Int): IntArray {
        val counts = IntArray(roles.size)
        var remaining = size
        for ((index, role) in roles.withIndex()) {
            if (remaining <= 0) {
                break
            }
            val min = minOf(role.min, role.max ?: Int.MAX_VALUE, remaining)
            counts[index] = min
            remaining -= min
        }
        while (remaining > 0) {
            val open = roles.indices.filter { roles[it].weight > 0 && counts[it] < (roles[it].max ?: Int.MAX_VALUE) }
            if (open.isEmpty()) {
                break
            }
            val total = open.sumOf { roles[it].weight }
            val shares = DoubleArray(roles.size)
            val add = IntArray(roles.size)
            var assigned = 0
            for (index in open) {
                shares[index] = remaining.toDouble() * roles[index].weight / total
                add[index] = shares[index].toInt()
                assigned += add[index]
            }
            var left = remaining - assigned
            for (index in open.sortedWith(compareByDescending<Int> { shares[it] - add[it] }.thenByDescending { roles[it].weight })) {
                if (left <= 0) {
                    break
                }
                add[index]++
                left--
            }
            var used = 0
            for (index in open) {
                val space = (roles[index].max ?: Int.MAX_VALUE) - counts[index]
                val amount = minOf(add[index], space)
                counts[index] += amount
                used += amount
            }
            if (used == 0) {
                break
            }
            remaining -= used
        }
        return counts
    }
}