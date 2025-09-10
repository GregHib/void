package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class HolyBiscuits {

    init {
        consume("holy_biscuits") { player ->
            player.levels.restore(Skill.Prayer, 10)
        }
    }
}
