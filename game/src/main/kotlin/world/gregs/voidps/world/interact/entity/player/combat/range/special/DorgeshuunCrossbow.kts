package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.world.interact.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.entity.sound.playSound

specialAttack("snipe") { player ->
    player.anim("crossbow_accurate")
    player.playSound("${id}_special")
    val time = player.shoot(id = "snipe_special", target = target)
    player.hit(target, delay = time)
}