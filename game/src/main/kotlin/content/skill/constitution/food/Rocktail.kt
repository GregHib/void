package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Rocktail : Script {

    init {
        consume("rocktail") { player ->
            val range: IntRange = item.def.getOrNull("heals") ?: return@consume
            val amount = range.random()
            player.levels.boost(Skill.Constitution, amount, maximum = 100)
            cancel()
        }
    }
}
