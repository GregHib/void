package content.bot.allocation

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

/**
 * A skill profile for spawned bots, each skill is rolled within its range.
 * E.g. a zerker build with attack 60-75, strength 80-99, defence 45
 */
data class Build(val id: String, val levels: Map<Skill, IntRange>) {
    fun roll(): Map<Skill, Int> = levels.mapValues { (_, range) -> if (range.last <= range.first) range.first else random.nextInt(range.first, range.last + 1) }
}
