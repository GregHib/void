package content.quest.miniquest.barbarian_training

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BarbarianHerblore : Script {
    init {
        crafted(Skill.Herblore) { def ->
            if (def.variable == "barbarian_training_herblore") {
                barbarianTaskDone("herblore")
            }
        }
    }
}
