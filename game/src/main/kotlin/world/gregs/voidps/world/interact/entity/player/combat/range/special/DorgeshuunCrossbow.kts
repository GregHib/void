package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

specialAttack("snipe") { player ->
    player.setAnimation("crossbow_accurate")
    val time = player.shoot(id = "snipe_special", target = target)
    player.hit(target, delay = time)
}