package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume("jug_of_wine") { player: Player ->
    player.levels.drain(Skill.Attack, 2)
}