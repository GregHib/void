package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item

/**
 * Damage done to a [target]
 * Emitted on swing, where [CombatDamage] is after the attack delay
 * @param type the combat type, typically: melee, range or magic
 * @param damage the damage inflicted upon the [target]
 * @param delay until hit in client ticks
 */
data class CombatAttack(
    val target: Character,
    val damage: Int,
    val type: String,
    val weapon: Item,
    val spell: String,
    val special: Boolean,
    val delay: Int,
)