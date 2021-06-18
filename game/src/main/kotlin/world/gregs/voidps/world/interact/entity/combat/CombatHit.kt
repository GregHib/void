package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Damage done by [source]
 */
data class CombatHit(
    val source: Character,
    val type: String,
    val damage: Int,
    val weapon: Item?,
    val special: Boolean
) : Event
