package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Damage done by [source]
 * @param type the combat type, typically: melee, range or magic
 * @param damage the damage inflicted by the [source]
 */
data class CombatHit(
    val source: Character,
    val type: String,
    val damage: Int,
    val weapon: Item?,
    val spell: String,
    val special: Boolean
) : Event {
    var blocked = false
}
