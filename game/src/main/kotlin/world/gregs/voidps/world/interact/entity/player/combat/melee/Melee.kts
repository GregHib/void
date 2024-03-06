package world.gregs.voidps.world.interact.entity.player.combat.melee

import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

combatPrepare("melee") { player ->
    val amount: Int? = player.weapon.def.getOrNull("special_energy")
    if (player.specialAttack && amount != null && !drainSpecialEnergy(player, amount)) {
        player.specialAttack = false
        cancel()
    }
}

val definitions: WeaponStyleDefinitions by inject()

combatSwing(style = "melee") { player ->
    val weapon = player.weapon.id
    when {
        weapon == "barrelchest_anchor" -> {
            player.setAnimation("anchor_attack")
            player.hit(target)
        }
        weapon.startsWith("dragon_dagger") || weapon.startsWith("corrupt_dragon_dagger") -> {
            player.setAnimation("dragon_dagger_${
                when (player.attackType) {
                    "slash" -> "slash"
                    else -> "attack"
                }
            }")
            player.hit(target)
        }
        weapon.startsWith("korasis_sword") -> {
            player.setAnimation("korasis_sword_${
                when (player.attackType) {
                    "chop" -> "chop"
                    else -> "slash"
                }
            }")
            player.hit(target)
        }
        weapon.startsWith("vestas_longsword") || weapon.startsWith("corrupt_vestas_longsword") -> {
            player.setAnimation("vestas_longsword_${
                when (player.attackType) {
                    "lunge" -> "lunge"
                    else -> "attack"
                }
            }")
            player.hit(target)
        }
        weapon.startsWith("banner") || weapon.startsWith("rat_pole") || weapon.endsWith("flag") -> {
            player.setAnimation("banner_attack")
            player.hit(target)
        }
        weapon.startsWith("boxing_gloves") -> {
            player.setAnimation("boxing_gloves_attack")
            player.hit(target)
        }
        weapon.startsWith("dharoks_greataxe") || weapon == "balmung" -> {
            player.setAnimation("dharoks_greataxe_${
                when (player.attackType) {
                    "smash" -> "smash"
                    else -> "attack"
                }
            }")
            player.hit(target)
        }
        weapon.startsWith("easter_carrot") -> {
            player.setAnimation("easter_carrot_whack")
            player.hit(target)
        }
        weapon.endsWith("godsword") || weapon.startsWith("saradomin_sword") -> {
            player.setAnimation("godsword_${player.attackType}")
            player.hit(target)
        }
        weapon == "golden_hammer" -> {
            player.setAnimation("tzhaar_ket_om_attack")
            player.hit(target)
        }
        weapon.startsWith("granite_maul") -> {
            player.setAnimation("granite_maul_attack")
            player.hit(target)
        }
        weapon.startsWith("guthans_warspear") -> {
            player.setAnimation("guthans_spear_${
                when (player.attackType) {
                    "swipe" -> "swipe"
                    else -> "attack"
                }
            }")
            player.hit(target)
        }
        weapon.startsWith("mouse_toy") -> {
            player.setAnimation("mouse_toy_attack")
            player.hit(target)
        }
        weapon.startsWith("torags_hammers") -> {
            player.setAnimation("torags_hammers_attack")
            player.hit(target)
        }
        weapon.startsWith("veracs_flail") -> {
            player.setAnimation("veracs_flail_attack")
            player.hit(target)
        }
        weapon.startsWith("zamorakian_spear") -> {
            player.setAnimation("zamorakian_spear_${
                when (player.attackType) {
                    "block" -> "lunge"
                    else -> player.attackType
                }
            }")
            player.hit(target)
        }
        else -> {
            val id = player.weapon.def["weapon_style", 0]
            val style = definitions.get(id)
            player.setAnimation("${style.stringId}_${player.attackType}")
            player.hit(target)
        }
    }
}