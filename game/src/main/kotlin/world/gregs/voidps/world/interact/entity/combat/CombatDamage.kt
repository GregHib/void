package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Damage done to a [target]
 * @param type the combat type, typically: melee, range or magic
 * @param damage the damage inflicted upon the [target]
 */
data class CombatDamage(
    val target: Character,
    val type: String,
    val damage: Int,
    val weapon: Item?,
    val special: Boolean
) : Event
