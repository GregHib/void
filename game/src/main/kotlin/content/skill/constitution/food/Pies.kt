package content.skill.constitution.food

import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Pies : Script {

    init {
        consumed("admiral_pie*") { _, _ ->
            levels.boost(Skill.Fishing, 5)
        }

        consumed("fish_pie*") { _, _ ->
            levels.boost(Skill.Fishing, 3)
        }

        consumed("garden_pie*") { _, _ ->
            levels.boost(Skill.Farming, 5)
        }

        consumed("summer_pie*") { _, _ ->
            runEnergy += (runEnergy / 100) * 10
            levels.boost(Skill.Agility, 5)
        }

        consumed("wild_pie*") { _, _ ->
            levels.boost(Skill.Slayer, 4)
            levels.boost(Skill.Ranged, 4)
        }
    }
}
