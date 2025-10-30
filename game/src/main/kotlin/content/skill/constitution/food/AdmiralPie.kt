package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class AdmiralPie : Script {

    init {
        consume("admiral_pie*") { player ->
            player.levels.boost(Skill.Fishing, 5)
        }
    }
}
