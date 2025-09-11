package content.skill.constitution.food

import content.entity.player.effect.energy.runEnergy
import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class SummerPie {

    init {
        consume("summer_pie*") { player ->
            player.runEnergy += (player.runEnergy / 100) * 10
            player.levels.boost(Skill.Agility, 5)
        }
    }
}
