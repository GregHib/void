package content.area.misthalin.barbarian_village.stronghold_of_security

import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class Catablepon : Script {

    init {
        npcCombatPrepare("catablepon*") { target ->
            if (random.nextBoolean() && target.levels.get(Skill.Strength) > 3 + (target.levels.getMax(Skill.Strength) * 0.92)) {
                anim("catablepon_attack_breath")
                spell = "weaken"
            } else {
                anim("catablepon_attack")
                spell = ""
            }
            true
        }
    }
}
