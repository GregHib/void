package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.throwHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isJavelin(item: Item?) = item != null && item.id.contains("_javelin")

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && isJavelin(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.id
    player.ammo = ""
    removeAmmo(player, target, ammo, required)
    player.ammo = ammo
}

on<CombatSwing>({ player -> !swung() && isJavelin(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo.removePrefix("corrupt_").removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
    player.setAnimation("throw_javelin")
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = throwHitDelay(distance))
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}