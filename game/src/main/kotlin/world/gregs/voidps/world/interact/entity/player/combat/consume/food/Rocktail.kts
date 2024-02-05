package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume({ item.id == "rocktail" }) { player: Player ->
    val range: IntRange = item.def.getOrNull("heals") ?: return@consume
    val amount = range.random()
    player.levels.boost(Skill.Constitution, amount, maximum = 100)
    cancel()
}