package content.skill.ranged

import content.entity.combat.characterCombatSwing
import content.entity.combat.combatPrepare
import content.entity.combat.combatSwing
import content.entity.combat.hit.hit
import content.entity.npc.combat.NPCAttack
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.entity.sound.sound
import content.skill.melee.weapon.attackType
import content.skill.melee.weapon.weapon
import content.skill.slayer.categories
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject

val weaponStyles: WeaponStyleDefinitions by inject()
val weaponDefinitions: WeaponAnimationDefinitions by inject()
val animationDefinitions: AnimationDefinitions by inject()

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
        character.anim(ammo)
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
                character.sound("dart_throw")
            } else if (weapon.contains("javelin")) {
                character.sound("javelin_throw")
            } else if (weapon.contains("knife")) {
                character.sound("knife_throw")
            } else if (weapon.contains("axe")) {
                character.sound("axe_throw")
            } else {
                character.sound("thrown")
            }
        }
        "bow" -> {
            character.gfx("${if (ammo.endsWith("brutal")) "brutal" else ammo}_shoot")
            if (weapon.contains("shortbow")) {
                character.sound("shortbow_shoot")
            } else {
                character.sound("longbow_shoot")
            }
        }
        "fixed_device" -> {
            // TODO
        }
        "salamander" -> {
            time = 0
            character.gfx("salamander_${character.attackType}")
        }
        "crossbow" -> character.sound("crossbow_shoot")
    }
    val type = character.weapon.def.getOrNull("weapon_type") ?: style.stringId
    var animation: String?
    if (character is NPC && !character.categories.contains("human")) {
        animation = NPCAttack.anim(animationDefinitions, character, "attack")
    } else {
        val definition = weaponDefinitions.get(type)
        animation = definition.attackTypes.getOrDefault(character.attackType, definition.attackTypes["default"])
        if (animation == null) {
            animation = "${style.stringId}_${character.attackType}"
        }
    }
    character.anim(animation)
    character.hit(target, delay = if (time == -1) 64 else time)
}
