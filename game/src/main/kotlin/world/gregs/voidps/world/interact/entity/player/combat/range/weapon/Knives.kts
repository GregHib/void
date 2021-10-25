package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isKnife(item: Item?) = item != null && item.id.contains("_knife")

on<CombatSwing>({ player -> !swung() && isKnife(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.id
    player.ammo = ""
    removeAmmo(player, target, ammo, required)
    player.ammo = ammo
}

on<CombatSwing>({ player -> !swung() && isKnife(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo.removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
    player.setAnimation("throw_projectile")
    player.setGraphic("${ammo}_throw")
    player.shoot(id = ammo, target = target)
    player.hit(target, delay = if (player.attackType == "rapid") 1 else 2)
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}