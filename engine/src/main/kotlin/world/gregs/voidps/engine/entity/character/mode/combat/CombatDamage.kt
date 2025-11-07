package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item

/**
 * Damage done by [source] to the emitter
 * Used for defend graphics, for effects use [CombatAttack]
 * @param type the combat type, typically: melee, range or magic
 * @param damage the damage inflicted by the [source]
 * @param weapon weapon used
 * @param spell magic spell used
 * @param special whether weapon special attack was used
 */
data class CombatDamage(
    val source: Character,
    val type: String,
    val damage: Int,
    val weapon: Item,
    val spell: String,
    val special: Boolean,
)