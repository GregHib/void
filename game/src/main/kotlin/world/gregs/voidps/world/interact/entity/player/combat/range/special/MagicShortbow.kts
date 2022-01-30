package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toInt
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.bowHitDelay
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isMagicShort(weapon: Item?) = weapon != null && weapon.id.startsWith("magic_shortbow")

on<CombatSwing>({ player -> isMagicShort(player.weapon) }, Priority.HIGHER) { player: Player ->
    player["required_ammo"] = player.specialAttack.toInt() + 1
}

on<CombatSwing>({ player -> !swung() && player.specialAttack && isMagicShort(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 550)) {
        delay = -1
        return@on
    }
    player.setAnimation("magic_shortbow_special")
    player.setGraphic("magic_shortbow_special")
    player.setGraphic("magic_shortbow_special", delay = 30)
    player.playSound("magic_shortbow_special")
    val distance = player.tile.distanceTo(target)
    player.shoot(id = "special_arrow", target = target, delay = 20, flightTime = 10 + distance * 3)
    player.shoot(id = "special_arrow", target = target, delay = 50, flightTime = distance * 3)
    player.hit(target, delay = bowHitDelay(distance))
    player.hit(target, delay = bowHitDelay(distance))
}