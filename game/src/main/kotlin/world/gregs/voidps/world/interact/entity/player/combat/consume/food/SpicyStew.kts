package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume({ item.id == "spicy_stew" }) { player: Player ->
    if (random.nextInt(100) > 5) {
        player.levels.boost(Skill.Cooking, 6)
    } else {
        player.levels.drain(Skill.Cooking, 6)
    }
}