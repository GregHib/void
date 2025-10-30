package content.skill.constitution.drink

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Cocktails : Script {

    init {
        consume("wizard_blizzard") { player ->
            player.levels.boost(Skill.Strength, 6)
            player.levels.drain(Skill.Attack, 4)
        }

        consume("short_green_guy") { player ->
            player.levels.boost(Skill.Strength, 4)
            player.levels.drain(Skill.Attack, 3)
        }

        consume("drunk_dragon") { player ->
            player.levels.boost(Skill.Strength, 5)
            player.levels.drain(Skill.Attack, 4)
        }

        consume("chocolate_saturday") { player ->
            player.levels.boost(Skill.Strength, 7)
            player.levels.drain(Skill.Attack, 4)
        }

        consume("blurberry_special") { player ->
            player.levels.boost(Skill.Strength, 6)
            player.levels.drain(Skill.Attack, 4)
        }
    }
}
