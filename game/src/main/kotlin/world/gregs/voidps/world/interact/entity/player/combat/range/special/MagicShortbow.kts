package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

weaponSwing("magic_shortbow*", style = "range", priority = Priority.HIGHER) { player ->
    player["required_ammo"] = player.specialAttack.toInt() + 1
}

specialAttackSwing("magic_shortbow*", style = "range", priority = Priority.MEDIUM) { player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 550)) {
        delay = -1
        return@specialAttackSwing
    }
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