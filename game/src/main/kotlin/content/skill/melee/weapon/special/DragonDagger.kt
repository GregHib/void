package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.engine.Script

class DragonDagger :
    Script,
    SpecialAttack {
    init {
        specialAttackDamage("puncture") { target, _ ->
            hit(target)
        }
    }
}
