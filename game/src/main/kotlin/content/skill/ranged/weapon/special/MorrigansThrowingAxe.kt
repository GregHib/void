package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start

class MorrigansThrowingAxe :
    Script,
    SpecialAttack {

    init {
        specialAttack("hamstring") { target, id ->
            val ammo = ammo
            anim("throw_morrigans_throwing_axe_special")
            gfx("${ammo}_special")
            val time = shoot(id = ammo, target = target, height = 15)
            if (hit(target, delay = time) != -1) {
                target.start(id, 100)
            }
        }
    }
}
