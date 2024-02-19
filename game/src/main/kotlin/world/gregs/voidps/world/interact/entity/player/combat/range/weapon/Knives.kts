package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.range.Ammo
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.proj.shoot

weaponSwing("*_knife*", style = "range", priority = Priority.HIGH) { player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.id
    player.ammo = ""
    Ammo.remove(player, target, ammo, required)
    player.ammo = ammo
}

weaponSwing("*_knife*", style = "range", priority = Priority.LOW) { player ->
    val ammo = player.ammo.removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
    player.setAnimation("thrown_accurate")
    player.setGraphic("${ammo}_throw")
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.throwDelay(distance))
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}