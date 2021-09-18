package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.math.floor

fun isDarkBow(weapon: Item?) = weapon != null && weapon.name.startsWith("dark_bow")

on<HitDamageModifier>({ type == "range" && special && isDarkBow(weapon) }, Priority.HIGH) { player: Player ->
    val dragon = player.ammo == "dragon_arrow"
    damage = floor(damage * if (dragon) 1.50 else 1.30).coerceAtLeast(if (dragon) 80.0 else 50.0)
}

on<CombatSwing>({ player -> !swung() && player.specialAttack && isDarkBow(player.weapon) }, Priority.HIGHISH) { player: Player ->
    val dragon = player.ammo == "dragon_arrow"
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 550)) {
        delay = -1
        return@on
    }
    player.setAnimation("bow_shoot")
    player.setGraphic("${player.ammo}_double_shot")
    player.playSound("dark_bow_special")
    player.playSound("descent_of_${if (dragon) "dragons" else "darkness"}")

    player.shoot("descent_of_arrow", target, true)
    player.shoot("arrow_smoke", target, true)
    if (dragon) {
        player.shoot("descent_of_dragons_head", target, true)
    }

    player.shoot("descent_of_arrow", target, false)
    player.shoot("arrow_smoke_2", target, false)
    if (dragon) {
        player.shoot("descent_of_dragons_head", target, false)
    }
    player.hit(target)
    player.hit(target)
}

on<CombatHit>({ source is Player && isDarkBow(weapon) && special }) { character: Character ->
    source as Player
    source.playSound("descent_of_darkness")
    source.playSound("descent_of_darkness", delay = 20)
    character.setGraphic("descent_of_${if (source.ammo == "dragon_arrow") "dragons" else "darkness"}_hit")
}

on<CombatSwing>({ player -> !swung() && isDarkBow(player.weapon) }, Priority.MEDIUM) { player: Player ->
    player.setAnimation("bow_shoot")
    val ammo = player.ammo
    player.setGraphic("${ammo}_double_shot")
    player.shoot(ammo, target, true)
    player.shoot(ammo, target, false)
    player.hit(target)
    player.hit(target)
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}

fun Player.shoot(name: String, target: Character, high: Boolean) {
    shoot(name = name, delay = 40, target = target, height = if (high) 43 else 40, flightTime = if (high) 40 else 30, curve = if (high) 18 else 8)
}