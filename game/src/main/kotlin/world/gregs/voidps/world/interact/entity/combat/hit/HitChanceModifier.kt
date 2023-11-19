package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Hit chance modifier
 * @param type the combat type, typically: melee, range or magic
 * @param chance chance of hitting 0.0-1.0
 */
data class HitChanceModifier(
    val target: Character,
    val type: String,
    var chance: Double,
    val weapon: Item,
    val special: Boolean
) : Event