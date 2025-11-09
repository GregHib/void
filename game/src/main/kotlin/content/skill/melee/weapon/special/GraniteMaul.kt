package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import content.entity.combat.target
import content.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.engine.Script

class GraniteMaul : Script {

    init {
        specialAttackPrepare("quick_smash") { id ->
            if (target == null) {
                return@specialAttackPrepare true
            }
            if (!SpecialAttack.drain(this)) {
                return@specialAttackPrepare false
            }
            val target = target ?: return@specialAttackPrepare false
            anim("${id}_special")
            gfx("${id}_special")
            hit(target)
            return@specialAttackPrepare false
        }
    }
}
