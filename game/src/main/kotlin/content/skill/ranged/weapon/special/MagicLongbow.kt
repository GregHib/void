package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.sound

class MagicLongbow : Script, SpecialAttack {

    init {
        specialAttack("powershot") { target, id ->
            anim("bow_accurate")
            gfx("special_arrow_shoot")
            sound("${id}_special")
            val time = shoot(id = "special_arrow", target = target)
            hit(target, delay = time)
        }
    }
}
