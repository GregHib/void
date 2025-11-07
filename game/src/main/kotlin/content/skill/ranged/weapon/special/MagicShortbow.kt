package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.distanceTo

class MagicShortbow :
    Script,
    SpecialAttack {

    init {
        specialAttack("snapshot") { target, id ->
            anim("${id}_special")
            gfx("${id}_special")
            gfx("${id}_special", delay = 30)
            sound("${id}_special")
            val distance = tile.distanceTo(target)
            val time1 = shoot(id = "special_arrow", target = target, delay = 20, flightTime = 10 + distance * 3)
            val time2 = shoot(id = "special_arrow", target = target, delay = 50, flightTime = distance * 3)
            hit(target, delay = time1)
            hit(target, delay = time2)
        }
    }
}
