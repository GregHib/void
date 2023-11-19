package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Event

/**
 * Overrides the effective level used in hit chance calculation
 * @param type the combat type, typically: melee, range or magic
 * @param defence whether calculating the attacker or defenders effective level
 * @param level the current effective level to replace
 */
data class HitEffectiveLevelOverride(
    val target: Character?,
    val type: String,
    val defence: Boolean,
    var level: Int
) : Event