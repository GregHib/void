package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.sound

class DorgeshuunCrossbow :
    Script,
    SpecialAttack {

    init {
        specialAttack("snipe") { target, id ->
            anim("crossbow_accurate")
            sound("${id}_special")
            val time = shoot(id = "snipe_special", target = target)
            hit(target, delay = time)
        }
    }
}
