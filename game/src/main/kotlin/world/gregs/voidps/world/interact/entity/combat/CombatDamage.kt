package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Damage done to [target]
 */
data class CombatDamage(val target: Character, val type: String, val damage: Int, val weapon: Item?) : Event
