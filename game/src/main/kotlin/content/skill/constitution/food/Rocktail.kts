package content.skill.constitution.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.skill.constitution.consume

consume("rocktail") { player ->
    val range: IntRange = item.def.getOrNull("heals") ?: return@consume
    val amount = range.random()
    player.levels.boost(Skill.Constitution, amount, maximum = 100)
    cancel()
}