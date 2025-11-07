package content.skill.prayer.active

import content.skill.prayer.praying
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Smite : Script {

    init {
        combatAttack { (target, damage) ->
            if (damage <= 40 || !praying("smite")) {
                return@combatAttack
            }
            target.levels.drain(Skill.Prayer, damage / 40)
        }
    }
}
