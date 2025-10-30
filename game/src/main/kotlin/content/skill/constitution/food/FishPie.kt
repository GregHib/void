package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class FishPie : Script {

    init {
        consume("fish_pie*") { player ->
            player.levels.boost(Skill.Fishing, 3)
        }
    }
}
