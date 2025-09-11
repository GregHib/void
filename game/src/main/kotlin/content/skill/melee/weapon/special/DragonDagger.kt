package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttackDamage
import world.gregs.voidps.engine.event.Script

@Script
class DragonDagger {

    init {
        specialAttackDamage("puncture", noHit = false) { player ->
            player.hit(target)
        }
    }
}
