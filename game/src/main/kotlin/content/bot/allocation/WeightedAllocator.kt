package content.bot.allocation

import content.bot.Bot

/**
 * Bots are given fixed [roles] in proportion to each role's weight, one spawn at a time.
 * Roles below their minimum are filled first in role order, then each bot goes to the role with the
 * fewest bots per weight that is still under its maximum (ties go to the higher weight, then role order).
 */
class WeightedAllocator(config: AllocationConfig, val roles: List<BotRole>) : BotAllocator(config) {

    override fun role(id: String): BotRole? = roles.firstOrNull { it.id == id }

    override fun allocate(members: List<Bot>): BotRole? {
        val current = roles.associate { role -> role.id to members.count { it.role == role.id } }
        val below = roles.firstOrNull { current.getValue(it.id) < minOf(it.min, it.max ?: Int.MAX_VALUE) }
        if (below != null) {
            return below
        }
        return roles
            .filter { it.weight > 0 && current.getValue(it.id) < (it.max ?: Int.MAX_VALUE) }
            .minWithOrNull(compareBy<BotRole> { current.getValue(it.id).toDouble() / it.weight }.thenByDescending { it.weight })
    }
}
