package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event

/**
 * Modify the level of a character before it is used in accuracy and damage combat formulas
 */
data class HitEffectiveLevelModifier(
    val skill: Skill,
    val accuracy: Boolean,
    var level: Double
) : Event