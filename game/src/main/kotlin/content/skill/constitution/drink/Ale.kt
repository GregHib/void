package content.skill.constitution.drink

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Ale : Script {

    init {
        consumed("bandits_brew") { _, _ ->
            levels.boost(Skill.Thieving, 1)
            levels.boost(Skill.Attack, 1)
            levels.drain(Skill.Strength, 3, 0.06)
            levels.drain(Skill.Defence, 3, 0.06)
        }

        consumed("beer") { _, _ ->
            levels.boost(Skill.Strength, 1, 0.02)
            levels.drain(Skill.Attack, 1, 0.06)
            set("dishwater_task", true)
        }

        consumed("keg_of_beer*") { _, _ ->
            levels.boost(Skill.Strength, 2, 0.10)
            levels.drain(Skill.Attack, 5, 0.50)
            timers.start("drunk") // TODO screen wobble until teleport
        }

        consumed("grog") { _, _ ->
            levels.boost(Skill.Strength, 3)
            levels.drain(Skill.Attack, 6)
        }
    }
}
