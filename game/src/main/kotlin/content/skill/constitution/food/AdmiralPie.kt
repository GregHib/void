package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class AdmiralPie {

    init {
        consume("admiral_pie*") { player ->
            player.levels.boost(Skill.Fishing, 5)
        }
    }
}
