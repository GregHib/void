package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class JugOfWine {

    init {
        consume("jug_of_wine") { player ->
            player.levels.drain(Skill.Attack, 2)
        }
    }
}
