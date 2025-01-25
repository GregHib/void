package content.skill.constitution.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.skill.constitution.consume

consume("jug_of_wine") { player ->
    player.levels.drain(Skill.Attack, 2)
}