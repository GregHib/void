package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event

/**
 * Overrides the effective level used in hit chance calculation
 * @param defence whether calculating the attacker or defender [skill] defence level
 */
data class HitChanceLevelOverride(
    val target: Character?,
    val skill: Skill,
    val defence: Boolean,
    var level: Int
) : Event