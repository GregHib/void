package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

specialAttack("snapshot") { player ->
    player.setAnimation("${id}_special")
    player.gfx("${id}_special")
    player.gfx("${id}_special", delay = 30)
    player.playSound("${id}_special")
    val distance = player.tile.distanceTo(target)
    val time1 = player.shoot(id = "special_arrow", target = target, delay = 20, flightTime = 10 + distance * 3)
    val time2 = player.shoot(id = "special_arrow", target = target, delay = 50, flightTime = distance * 3)
    player.hit(target, delay = time1)
    player.hit(target, delay = time2)
}