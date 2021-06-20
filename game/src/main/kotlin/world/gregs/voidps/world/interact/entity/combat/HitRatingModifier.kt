package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Hit offensive and defence rating modifier
 * @param offense whether calculating the attacker or defender rating
 */
data class HitRatingModifier(
    val target: Character?,
    val type: String,
    val offense: Boolean,
    var rating: Double,
    val weapon: Item?
) : Event