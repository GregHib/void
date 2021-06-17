package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Event

/**
 * Overrides the effective level used in hit chance calculation
 * @param defence whether calculating the attacker or defender defence level
 */
data class HitEffectiveLevelOverride(
    val target: Character?,
    val type: String,
    val defence: Boolean,
    var level: Int
) : Event