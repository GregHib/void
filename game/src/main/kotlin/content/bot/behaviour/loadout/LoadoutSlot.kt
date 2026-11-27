package content.bot.behaviour.loadout

import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.Condition

/**
 * An equipment slot or inventory entry in a [Loadout], either a set of explicit [ids] or a [gear] list to pick from.
 * [optional] slots are filled when possible but never stop an activity from starting.
 */
data class LoadoutSlot(
    val ids: Set<String> = emptySet(),
    val gear: Gear? = null,
    val min: Int? = null,
    val max: Int? = null,
    val optional: Boolean = false,
    val usable: Boolean = false,
    val equippable: Boolean = false,
) {
    val candidates: Collection<String>
        get() = gear?.ids ?: ids

    fun item(ids: Set<String>) = BotItem(ids, min, max, usable, equippable)

    companion object {
        fun parse(map: Map<String, Any>, gear: (String) -> Gear?, debug: () -> String): LoadoutSlot {
            val id = map["id"] as? String
            val gearId = map["gear"] as? String
            require((id == null) != (gearId == null)) { "Expected one of 'id' or 'gear' in ${debug()}." }
            var min = map["min"] as? Int
            val max = map["max"] as? Int
            if (min == null && max == null) {
                min = 1
            }
            return LoadoutSlot(
                ids = if (id != null) Condition.toIds(id) else emptySet(),
                gear = if (gearId != null) gear(gearId) ?: error("Unknown gear '$gearId' in ${debug()}.") else null,
                min = min,
                max = max,
                optional = map["optional"] as? Boolean ?: false,
                usable = map["usable"] as? Boolean ?: false,
                equippable = map["equippable"] as? Boolean ?: false,
            )
        }
    }
}
