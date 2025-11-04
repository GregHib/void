package content.skill.constitution.food

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class JugOfWine : Script {

    init {
        consumed("jug_of_wine") { _, _ ->
            levels.drain(Skill.Attack, 2)
        }
    }
}
