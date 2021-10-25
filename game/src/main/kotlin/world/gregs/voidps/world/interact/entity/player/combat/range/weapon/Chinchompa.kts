package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isChinchompa(item: Item?) = item != null && item.id.endsWith("chinchompa")

on<HitChanceModifier>({ player -> player != target && type == "range" && isChinchompa(weapon) }, Priority.HIGHEST) { player: Player ->
    val distance = player.tile.distanceTo(target ?: return@on)
    chance = when (player.attackType) {
        "short_fuse" -> when {
            distance <= 3 -> 1.0
            distance <= 6 -> 0.75
            else -> 0.5
        }
        "medium_fuse" -> when {
            distance <= 3 -> 0.75
            distance <= 6 -> 1.0
            else -> 0.75
        }
        "long_fuse" -> when {
            distance <= 3 -> 0.5
            distance <= 6 -> 0.75
            else -> 1.0
        }
        else -> 0.0
    }
}

on<CombatSwing>({ player -> !swung() && isChinchompa(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.id
    player.ammo = ""
    if (!player.equipment.remove(ammo, required)) {
        player.message("That was your last one!")
        delay = -1
        return@on
    }
    player.ammo = ammo
}

on<CombatSwing>({ player -> !swung() && isChinchompa(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo
    player.setAnimation("throw_chinchompa")
    player.shoot(id = ammo, target = target)
    player.hit(target)
    delay = player["attack_speed", 4] - if (player.attackType == "medium_fuse") 1 else 0
}

on<CombatHit>({ source is Player && isChinchompa(weapon) }) { character: Character ->
    source as Player
    source.playSound("chinchompa_explode", delay = 40)
    character.setGraphic("chinchompa_hit")
}

multiTargetHit({ isChinchompa(weapon) }, { if (it is Player) 9 else 11 })