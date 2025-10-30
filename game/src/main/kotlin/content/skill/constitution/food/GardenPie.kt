package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class GardenPie : Script {

    init {
        consume("garden_pie*") { player ->
            player.levels.boost(Skill.Farming, 5)
        }
    }
}
