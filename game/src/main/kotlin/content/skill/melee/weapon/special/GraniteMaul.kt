package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import content.entity.combat.target
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttackPrepare
import world.gregs.voidps.engine.event.Script
@Script
class GraniteMaul {

    init {
        specialAttackPrepare("quick_smash") { player ->
            if (player.target == null) {
                return@specialAttackPrepare
            }
            cancel()
            if (!SpecialAttack.drain(player)) {
                return@specialAttackPrepare
            }
            val target = player.target ?: return@specialAttackPrepare
            player.anim("${id}_special")
            player.gfx("${id}_special")
            player.hit(target)
        }

    }

}
