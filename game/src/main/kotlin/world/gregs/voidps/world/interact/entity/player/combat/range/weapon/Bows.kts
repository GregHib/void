package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isBow(item: Item) = (item.id.endsWith("bow") && !item.id.endsWith("crossbow")) || item.id == "seercull" || item.id.endsWith("longbow_sighted")

combatSwing({ player -> !swung() && isBow(player.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("bow_accurate")
    val ammo = player.ammo
    player.setGraphic("${if (ammo.endsWith("brutal")) "brutal" else ammo}_shoot")
    player.shoot(id = if (ammo.endsWith("brutal")) "brutal_arrow" else ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = 1 + (3 + distance) / 6)
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}