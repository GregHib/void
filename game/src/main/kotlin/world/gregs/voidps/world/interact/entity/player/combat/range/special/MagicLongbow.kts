package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

specialAttack("powershot") { player ->
    player.setAnimation("bow_accurate")
    player.gfx("special_arrow_shoot")
    player.playSound("${id}_special")
    val time = player.shoot(id = "special_arrow", target = target)
    player.hit(target, delay = time)
}