package content.minigame.sorceress_garden

import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class SqirkJuice : Script {

    init {
        consumed("winter_sqirkjuice") { _, _ ->
            runEnergy += runEnergy / 20
        }

        consumed("spring_sqirkjuice") { _, _ ->
            runEnergy += runEnergy / 10
            levels.boost(Skill.Thieving, 1)
        }

        consumed("autumn_sqirkjuice") { _, _ ->
            runEnergy += (runEnergy * 0.15).toInt()
            levels.boost(Skill.Thieving, 2)
        }

        consumed("summer_sqirkjuice") { _, _ ->
            runEnergy += runEnergy / 5
            levels.boost(Skill.Thieving, 3)
        }
    }
}
