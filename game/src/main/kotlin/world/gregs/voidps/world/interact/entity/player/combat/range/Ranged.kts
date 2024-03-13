package world.gregs.voidps.world.interact.entity.player.combat.range

import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

val weaponStyles: WeaponStyleDefinitions by inject()
val animationDefinitions: WeaponAnimationDefinitions by inject()

combatPrepare("range") { player ->
    if (player.specialAttack && !SpecialAttack.hasEnergy(player)) {
        cancel()
    }
}

combatSwing(style = "scorch") { player ->
    extracted(player, target)
}

combatSwing(style = "range") { player ->
    extracted(player, target)
}

fun extracted(player: Player, target: Character) {
    var ammo = player.ammo
    val required = Ammo.requiredAmount(player.weapon, player.specialAttack)
    if (player.specialAttack && SpecialAttack.drain(player)) {
        val id: String = player.weapon.def.getOrNull("special") ?: return
        player.emit(SpecialAttack(id, target))
        return
    }
    val style = weaponStyles.get(player.weapon.def["weapon_style", 0])
    if (style.stringId != "sling") {
        Ammo.remove(player, target, ammo, required)
    } else {
        player.setAnimation(ammo)
    }
    if (style.stringId == "crossbow") {
        ammo = if (ammo == "barbed_bolts" || ammo == "bone_bolts" || ammo == "hand_cannon_shot") ammo else "crossbow_bolt"
    } else if (style.stringId == "bow" && ammo.endsWith("brutal")) {
        ammo = "brutal_arrow"
    }
    var time = player.shoot(id = ammo, target = target)
    val weapon = player.weapon.id
    when (style.stringId) {
        "thrown" -> {
            val ammoName = player.ammo.removePrefix("corrupt_").removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
            player.setGraphic("${ammoName}_throw")
            if (weapon.contains("dart")) {
                player.playSound("dart_throw")
            } else if (weapon.contains("javelin")) {
                player.playSound("javelin_throw")
            } else if (weapon.contains("knife")) {
                player.playSound("knife_throw")
            } else if (weapon.contains("axe")) {
                player.playSound("axe_throw")
            } else {
                player.playSound("thrown")
            }
        }
        "bow" -> {
            player.setGraphic("${if (ammo.endsWith("brutal")) "brutal" else ammo}_shoot")
            if (weapon.contains("shortbow")) {
                player.playSound("shortbow_shoot")
            } else {
                player.playSound("longbow_shoot")
            }
        }
        "fixed_device" -> {
            // TODO
        }
        "salamander" -> {
            time = 0
            player.setGraphic("salamander_${player.attackType}")
        }
        "crossbow" -> player.playSound("crossbow_shoot")
    }
    val type = player.weapon.def.getOrNull("weapon_type") ?: style.stringId
    val definition = animationDefinitions.get(type)
    var animation = definition.attackTypes.getOrDefault(player.attackType, definition.attackTypes["default"])
    if (animation == null) {
        animation = "${style.stringId}_${player.attackType}"
    }
    player.setAnimation(animation)
    player.hit(target, delay = if (time == -1) 64 else time)
}