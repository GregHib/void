package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class JugOfWine : Script {

    init {
        consume("jug_of_wine") { player ->
            player.levels.drain(Skill.Attack, 2)
        }
    }
}
