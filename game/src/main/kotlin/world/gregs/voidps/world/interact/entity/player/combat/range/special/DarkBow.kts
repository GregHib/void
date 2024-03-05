package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

specialAttackSwing("dark_bow*", style = "range", priority = Priority.HIGHISH) { player ->
    val dragon = player.ammo == "dragon_arrow"
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 550)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("bow_accurate")
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

characterCombatHit("dark_bow*", "range") { character ->
    source.playSound("descent_of_darkness")
    source.playSound("descent_of_darkness", delay = 20)
    character.setGraphic("descent_of_${if (source.ammo == "dragon_arrow") "dragons" else "darkness"}_hit")
}

weaponSwing("dark_bow*", style = "range", priority = Priority.MEDIUM) { player ->
    player.setAnimation("bow_accurate")
    val ammo = player.ammo
    player.setGraphic("${ammo}_double_shot")
    player.shoot(ammo, target, true)
    player.shoot(ammo, target, false)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.darkBowDelay(distance))
    player.hit(target, delay = Hit.darkBowDelay(distance))
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}

fun Player.shoot(id: String, target: Character, high: Boolean) {
    val distance = tile.distanceTo(target)
    shoot(id = id, delay = 41, target = target, height = if (high) 43 else 40, flightTime = (if (high) 14 else 5) + distance * 10, curve = if (high) 25 else 5)
}