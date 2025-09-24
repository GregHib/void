package content.minigame.sorceress_garden

import content.entity.player.effect.energy.runEnergy
import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class SqirkJuice {
    init {
        consume("winter_sqirkjuice") { player ->
            player.runEnergy += player.runEnergy / 20
        }

        consume("spring_sqirkjuice") { player ->
            player.runEnergy += player.runEnergy / 10
            player.levels.boost(Skill.Thieving, 1)
        }

        consume("autumn_sqirkjuice") { player ->
            player.runEnergy += (player.runEnergy * 0.15).toInt()
            player.levels.boost(Skill.Thieving, 2)
        }

        consume("summer_sqirkjuice") { player ->
            player.runEnergy += player.runEnergy / 5
            player.levels.boost(Skill.Thieving, 3)
        }
    }
}
