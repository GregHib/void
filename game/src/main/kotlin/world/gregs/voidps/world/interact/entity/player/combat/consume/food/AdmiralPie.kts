package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume({ item.id.startsWith("admiral_pie") }) { player: Player ->
    player.levels.boost(Skill.Fishing, 5)
}