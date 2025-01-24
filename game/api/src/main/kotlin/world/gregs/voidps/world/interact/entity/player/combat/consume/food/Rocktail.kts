package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume("rocktail") { player ->
    val range: IntRange = item.def.getOrNull("heals") ?: return@consume
    val amount = range.random()
    player.levels.boost(Skill.Constitution, amount, maximum = 100)
    cancel()
}