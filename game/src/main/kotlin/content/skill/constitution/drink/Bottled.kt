package content.skill.constitution.drink

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Bottled : Script {

    init {
        consumed("karamjan_rum") { _, _ ->
            levels.boost(Skill.Strength, 5)
            levels.drain(Skill.Attack, 4)
        }

        consumed("vodka,gin,brandy,whisky") { _, _ ->
            levels.boost(Skill.Strength, 1, 0.05)
            levels.drain(Skill.Attack, 3, 0.02)
        }

        consumed("bottle_of_wine") { _, _ ->
            levels.drain(Skill.Attack, 3)
        }

        consumed("braindeath_rum") { _, _ ->
            levels.boost(Skill.Strength, 3)
            levels.boost(Skill.Mining, 1)
            levels.drain(Skill.Defence, multiplier = 0.10)
            levels.drain(Skill.Attack, multiplier = 0.05)
            levels.drain(Skill.Prayer, multiplier = 0.05)
            levels.drain(Skill.Ranged, multiplier = 0.05)
            levels.drain(Skill.Magic, multiplier = 0.05)
            levels.drain(Skill.Agility, multiplier = 0.05)
            levels.drain(Skill.Herblore, multiplier = 0.05)
        }
    }
}
