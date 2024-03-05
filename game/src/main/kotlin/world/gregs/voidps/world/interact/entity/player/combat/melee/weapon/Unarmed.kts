package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

val definitions: WeaponStyleDefinitions by inject()

combatSwing { player ->
    val weapon = target.weapon.id
    when {
        weapon.startsWith("banner") || weapon.startsWith("rat_pole") || weapon.endsWith("flag") -> {
            player.setAnimation("banner_attack")
            player.hit(target)
            delay = 4
        }
        weapon.startsWith("boxing_gloves") -> {
            player.setAnimation("boxing_gloves_attack")
            player.hit(target)
            delay = 4
        }
        weapon.startsWith("dharoks_greataxe") || weapon == "balmung" -> {
            player.setAnimation("dharoks_greataxe_${
                when (player.attackType) {
                    "smash" -> "smash"
                    else -> "attack"
                }
            }")
            player.hit(target)
            delay = 7
        }
        weapon.startsWith("easter_carrot") -> {
            player.setAnimation("easter_carrot_whack")
            player.hit(target)
            delay = 6
        }
        weapon.endsWith("godsword") || weapon.startsWith("saradomin_sword") -> {
            player.setAnimation("godsword_${player.attackType}")
            player.hit(target)
            delay = 6
        }
        weapon == "golden_hammer" -> {
            player.setAnimation("tzhaar_ket_om_attack")
            player.hit(target)
            delay = 6
        }
        weapon.startsWith("granite_maul") -> {
            player.setAnimation("granite_maul_attack")
            player.hit(target)
            delay = 7
        }
        weapon.startsWith("guthans_warspear") -> {
            player.setAnimation("guthans_spear_${
                when (player.attackType) {
                    "swipe" -> "swipe"
                    else -> "attack"
                }
            }")
            player.hit(target)
            delay = 5
        }
        weapon.startsWith("mouse_toy") -> {
            player.setAnimation("mouse_toy_attack")
            player.hit(target)
            delay = 4
        }
        weapon.startsWith("torags_hammers") -> {
            player.setAnimation("torags_hammers_attack")
            player.hit(target)
            delay = 5
        }
        weapon.startsWith("veracs_flail") -> {
            player.setAnimation("veracs_flail_attack")
            player.hit(target)
            delay = 5
        }
        weapon.startsWith("zamorakian_spear") -> {
            player.setAnimation("zamorakian_spear_${
                when (player.attackType) {
                    "block" -> "lunge"
                    else -> player.attackType
                }
            }")
            player.hit(target)
            delay = 4
        }
        else -> {
            val id = player.weapon.def["weapon_style", 0]
            val style = definitions.get(id)
            player.setAnimation("${style.stringId}_${player.attackType}")
            player.hit(target)
            delay = player.weapon.def["attack_speed", 4]
        }
    }
}