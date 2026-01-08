package content.area.misthalin.barbarian_village.stronghold_of_security

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Catablepon : Script {
    init {
        npcCondition("catablepon") { target ->
            target.levels.get(Skill.Strength) > 3 + (target.levels.getMax(Skill.Strength) * 0.92)
        }
    }
}
