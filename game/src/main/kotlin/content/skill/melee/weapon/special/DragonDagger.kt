package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import world.gregs.voidps.engine.Script

class DragonDagger : Script {
    init {
        specialAttackDamage("puncture") { target, _ ->
            hit(target)
        }
    }
}
