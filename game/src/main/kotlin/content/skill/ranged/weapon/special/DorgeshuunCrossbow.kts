package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.entity.sound.playSound

specialAttack("snipe") { player ->
    player.anim("crossbow_accurate")
    player.playSound("${id}_special")
    val time = player.shoot(id = "snipe_special", target = target)
    player.hit(target, delay = time)
}