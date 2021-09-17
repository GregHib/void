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

fun isSling(item: Item?) = item != null && item.name.endsWith("sling")

on<CombatSwing>({ player -> !swung() && isSling(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo
    player.setAnimation(ammo)
    player.setGraphic("${ammo}_throw", height = 100)
    player.shoot(name = ammo, target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}