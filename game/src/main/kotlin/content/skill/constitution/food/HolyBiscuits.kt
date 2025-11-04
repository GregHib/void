package content.skill.constitution.food

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class HolyBiscuits : Script {

    init {
        consumed("holy_biscuits") { _, _ ->
            levels.restore(Skill.Prayer, 10)
        }
    }
}
