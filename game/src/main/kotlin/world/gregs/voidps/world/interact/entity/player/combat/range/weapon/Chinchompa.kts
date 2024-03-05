package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isChinchompa(item: Item) = item.id.endsWith("chinchompa")

weaponSwing("*chinchompa", style = "range", priority = Priority.HIGH) { player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.id
    player.ammo = ""
    if (!player.equipment.remove(ammo, required)) {
        player.message("That was your last one!")
        delay = -1
        return@weaponSwing
    }
    player.ammo = ammo
}

weaponSwing("*chinchompa", style = "range", priority = Priority.LOW) { player ->
    val ammo = player.ammo
    player.setAnimation("chinchompa_short_fuse")
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.throwDelay(distance))
    delay = player["attack_speed", 4] - if (player.attackType == "medium_fuse") 1 else 0
}

characterCombatHit("*chinchompa", "range") { character ->
    source as Player
    source.playSound("chinchompa_explode", delay = 40)
    character.setGraphic("chinchompa_hit")
}

multiTargetHit({ isChinchompa(weapon) }, { if (it is Player) 9 else 11 })