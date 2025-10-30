package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class HolyBiscuits : Script {

    init {
        consume("holy_biscuits") { player ->
            player.levels.restore(Skill.Prayer, 10)
        }
    }
}
