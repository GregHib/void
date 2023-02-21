package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Damage done to a [target]
 * Emitted on swing, where [CombatHit] is after the attack delay
 * @param type the combat type, typically: melee, range or magic
 * @param damage the damage inflicted upon the [target]
 */
data class CombatAttack(
    val target: Character,
    val type: String,
    val damage: Int,
    val weapon: Item?,
    val spell: String,
    val special: Boolean,
    val delay: Int
) : Event {
    var blocked = false
}
