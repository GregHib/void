package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isThrowingAxe(item: Item?) = item != null && item.name.contains("_throwing_axe")

on<CombatSwing>({ player -> !swung() && isThrowingAxe(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.name
    player.ammo = ""
    if (!player.equipment.remove(ammo, required)) {
        player.message("That was your last one!")
        delay = -1
        return@on
    }
    player.ammo = ammo
}

on<CombatSwing>({ player -> !swung() && isThrowingAxe(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo.removePrefix("corrupt_")
    player.setAnimation(if (ammo.contains("morrigans")) "throw_javelin" else "throw_projectile")
    player.setGraphic("${ammo}_throw")
    player.shoot(name = ammo, target = target, delay = 40)
    player.hit(target)
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}