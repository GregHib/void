package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

combatSwing("magic_shortbow*", style = "range", special = true) { player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    player.setAnimation("magic_shortbow_special")
    player.setGraphic("magic_shortbow_special")
    player.setGraphic("magic_shortbow_special", delay = 30)
    player.playSound("magic_shortbow_special")
    val distance = player.tile.distanceTo(target)
    player.shoot(id = "special_arrow", target = target, delay = 20, flightTime = 10 + distance * 3)
    player.shoot(id = "special_arrow", target = target, delay = 50, flightTime = distance * 3)
    player.hit(target, delay = Hit.bowDelay(distance))
    player.hit(target, delay = Hit.bowDelay(distance))
}