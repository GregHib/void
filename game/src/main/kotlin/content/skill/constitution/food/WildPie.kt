package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class WildPie : Script {

    init {
        consume("wild_pie*") { player ->
            player.levels.boost(Skill.Slayer, 4)
            player.levels.boost(Skill.Ranged, 4)
        }
    }
}
