package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isBow(item: Item) = (item.name.endsWith("bow") && !item.name.endsWith("crossbow")) || item.name == "seercull" || item.name.endsWith("longbow_sighted")

on<CombatSwing>({ player -> !swung() && isBow(player.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("bow_shoot")
    val ammo = player.ammo
    player.setGraphic("${if (ammo.endsWith("brutal")) "brutal" else ammo}_shoot")
    player.shoot(name = if (ammo.endsWith("brutal")) "brutal_arrow" else ammo, target = target, delay = 40, height = if (ammo.contains("ogre") || ammo.endsWith("brutal")) 40 else 43, endHeight = target.height, curve = 8)
    player.hit(target)
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}