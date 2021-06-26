package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.func.toInt
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isMagicShort(weapon: Item?) = weapon != null && weapon.name.startsWith("magic_shortbow")

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
    player.setGraphic("magic_shortbow_special", height = 100)
    player.setGraphic("magic_shortbow_special", height = 100, delay = 30)
    player.playSound("magic_shortbow_special")
    player.shoot(name = "special_arrow", target = target, delay = 30, height = 43, endHeight = target.height, curve = 8)
    player.shoot(name = "special_arrow", target = target, delay = 55, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
    player.hit(target)
}