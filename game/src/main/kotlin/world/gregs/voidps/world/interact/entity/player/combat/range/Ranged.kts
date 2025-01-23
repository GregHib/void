package world.gregs.voidps.world.interact.entity.player.combat.range

import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.hit
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
    swing(player, target)
}

characterCombatSwing(style = "range") { character ->
    swing(character, target)
}

fun swing(character: Character, target: Character) {
    var ammo = character.ammo
    val style = if (character is NPC) weaponStyles.get(character.def["weapon_style", "unarmed"]) else weaponStyles.get(character.weapon.def["weapon_style", 0])
    if (character is Player) {
        val required = Ammo.requiredAmount(character.weapon, character.specialAttack)
        if (character.specialAttack && SpecialAttack.drain(character)) {
            val id: String = character.weapon.def.getOrNull("special") ?: return
            character.emit(SpecialAttack(id, target))
            return
        }
        if (style.stringId != "sling") {
            Ammo.remove(character, target, ammo, required)
        }
    }
    if (style.stringId == "sling") {
        character.setAnimation(ammo)
    }
    if (style.stringId == "crossbow") {
        ammo = if (ammo == "barbed_bolts" || ammo == "bone_bolts" || ammo == "hand_cannon_shot") ammo else "crossbow_bolt"
    } else if (style.stringId == "bow" && ammo.endsWith("brutal")) {
        ammo = "brutal_arrow"
    }
    var time = character.shoot(id = ammo, target = target)
    val weapon = character.weapon.id
    when (style.stringId) {
        "thrown" -> {
            val ammoName = character.ammo.removePrefix("corrupt_").removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
            character.gfx("${ammoName}_throw")
            if (weapon.contains("dart")) {
                character.playSound("dart_throw")
            } else if (weapon.contains("javelin")) {
                character.playSound("javelin_throw")
            } else if (weapon.contains("knife")) {
                character.playSound("knife_throw")
            } else if (weapon.contains("axe")) {
                character.playSound("axe_throw")
            } else {
                character.playSound("thrown")
            }
        }
        "bow" -> {
            character.gfx("${if (ammo.endsWith("brutal")) "brutal" else ammo}_shoot")
            if (weapon.contains("shortbow")) {
                character.playSound("shortbow_shoot")
            } else {
                character.playSound("longbow_shoot")
            }
        }
        "fixed_device" -> {
            // TODO
        }
        "salamander" -> {
            time = 0
            character.gfx("salamander_${character.attackType}")
        }
        "crossbow" -> character.playSound("crossbow_shoot")
    }
    val type = character.weapon.def.getOrNull("weapon_type") ?: style.stringId
    val definition = animationDefinitions.get(type)
    var animation = definition.attackTypes.getOrDefault(character.attackType, definition.attackTypes["default"])
    if (animation == null) {
        animation = "${style.stringId}_${character.attackType}"
    }
    character.setAnimation(animation)
    character.hit(target, delay = if (time == -1) 64 else time)
}