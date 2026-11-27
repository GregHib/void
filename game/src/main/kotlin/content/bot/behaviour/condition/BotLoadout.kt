package content.bot.behaviour.condition

import content.bot.behaviour.loadout.Loadout
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

/**
 * Setup requirement to be wearing one of a weighted choice of [Loadout]s.
 * Replaced with concrete equipment and inventory conditions when an activity is assigned (see [content.bot.BotManager.bind]),
 * [check] is only a fallback for unbound use.
 */
data class BotLoadout(val options: List<Pair<Loadout, Int>>) : Condition(90) {
    override fun keys() = options.flatMapTo(mutableSetOf()) { (loadout, _) -> loadout.slots.values.flatMap { slot -> slot.candidates.map { "item:$it" } } }
    override fun events() = setOf("inv:worn_equipment")

    override fun check(player: Player): Boolean = options.any { (loadout, _) -> wearing(player, loadout) }

    private fun wearing(player: Player, loadout: Loadout): Boolean {
        for ((slot, spec) in loadout.slots) {
            if (spec.optional) {
                continue
            }
            val id = player.equipped(slot).id
            val candidates = spec.candidates
            if (!candidates.contains(id) && !(id.isEmpty() && candidates.contains("empty"))) {
                return false
            }
        }
        return contains(player, player.inventory, loadout.inventory)
    }

    /**
     * The [preferred] loadout if it's an option [player] qualifies for, otherwise a weighted random pick of the loadouts
     * [player] qualifies for, or of all loadouts when [any] and none qualify
     */
    fun pick(player: Player, any: Boolean = false, preferred: String? = null): Loadout? {
        if (preferred != null) {
            val loadout = options.firstOrNull { it.first.id == preferred }?.first
            if (loadout != null && loadout.qualifies(player)) {
                return loadout
            }
        }
        val qualifying = options.filter { (loadout, _) -> loadout.qualifies(player) }.ifEmpty { if (any) options else return null }
        return weighted(qualifying)
    }

    companion object {
        internal fun weighted(options: List<Pair<Loadout, Int>>): Loadout? {
            if (options.size <= 1) {
                return options.firstOrNull()?.first
            }
            val total = options.sumOf { it.second.coerceAtLeast(0) }
            if (total <= 0) {
                return options.first().first
            }
            var roll = random.nextInt(total)
            for ((loadout, weight) in options) {
                roll -= weight.coerceAtLeast(0)
                if (roll < 0) {
                    return loadout
                }
            }
            return options.last().first
        }
    }
}

/**
 * Requirement that a bot has the levels to use at least one of [loadouts].
 * Added automatically to the requirements of activities with a [BotLoadout] setup.
 */
data class BotLoadoutQualified(val loadouts: List<Loadout>) : Condition(1) {
    private val skills by lazy { loadouts.flatMapTo(mutableSetOf()) { it.skills }.map { "skill:${it.name.lowercase()}" }.toSet() }
    override fun keys() = skills
    override fun events() = skills
    override fun check(player: Player) = loadouts.any { it.qualifies(player) }
}
