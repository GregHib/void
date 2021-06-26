package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event

/**
 * Modify the level of a character before it is used in accuracy and damage combat formulas
 * @param skill the skill that the effective level is of
 * @param accuracy whether this effective level is to be used in the accuracy or damage calculation
 * @param level The current effective level
 */
data class HitEffectiveLevelModifier(
    val skill: Skill,
    val accuracy: Boolean,
    var level: Double
) : Event