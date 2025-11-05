package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.Script

class DorgeshuunCrossbow : Script {

    init {
        specialAttack("snipe") { player ->
            player.anim("crossbow_accurate")
            player.sound("${id}_special")
            val time = player.shoot(id = "snipe_special", target = target)
            player.hit(target, delay = time)
        }
    }
}
