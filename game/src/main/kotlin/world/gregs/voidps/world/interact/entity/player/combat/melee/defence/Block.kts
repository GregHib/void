package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.world.interact.entity.combat.weapon

val definitions: WeaponStyleDefinitions by inject()

characterCombatAttack { character ->
    if (target is Player) {
        val shield = target.equipped(EquipSlot.Shield).id
        if (shield.startsWith("boxing_gloves")) {
            target.setAnimation("boxing_gloves_block", delay)
        } else if (shield.endsWith("shield")) {
            target.setAnimation("shield_block", delay)
        } else if (shield.endsWith("defender")) {
            target.setAnimation("defender_block", delay)
        } else if (shield.endsWith("book")) {
            target.setAnimation("book_block", delay)
        } else {
            val weapon = target.weapon.id
            when {
                weapon.endsWith("godsword") || weapon.startsWith("saradomin_sword") -> target.setAnimation("godsword_hit", delay)
                weapon.startsWith("dharoks_greataxe") || weapon == "balmung" -> target.setAnimation("dharoks_greataxe_block", delay)
                weapon.startsWith("dragon_dagger") || weapon.startsWith("corrupt_dragon_dagger") -> target.setAnimation("dragon_dagger_block", delay)
                weapon.startsWith("banner") || weapon.startsWith("rat_pole") || weapon.endsWith("flag") -> target.setAnimation("banner_hit", delay)
                weapon.startsWith("abyssal_whip") || weapon.startsWith("mouse_toy") -> target.setAnimation("whip_block", delay)
                weapon.startsWith("barrelchest_anchor") -> target.setAnimation("anchor_block", delay)
                weapon.startsWith("granite_maul") -> target.setAnimation("granite_maul_block", delay)
                weapon.startsWith("guthans_warspear") -> target.setAnimation("guthans_spear_block", delay)
                weapon.startsWith("veracs_flail") -> target.setAnimation("veracs_flail_block", delay)
                weapon.startsWith("zamorakian_spear") -> target.setAnimation("zamorakian_spear_block", delay)
                weapon.startsWith("korasis_sword") -> target.setAnimation("korasis_sword_block", delay)
                weapon.endsWith("vestas_longsword") -> target.setAnimation("vestas_longsword_block", delay)
                weapon == "golden_hammer" -> target.setAnimation("tzhaar_ket_om_block", delay)
                else -> {
                    val id = target.weapon.def["weapon_style", -1]
                    if (id != -1) {
                        val style = definitions.get(id)
                        target.setAnimation("${style.stringId}_hit", delay)
                    } else {
                        target.setAnimation("player_block")
                    }
                }
            }
        }
        blocked = true
    } else if (target is NPC) {
        val animation = if (target.race.isNotEmpty()) "${target.race}_hit" else target.def.getOrNull("hit_anim") ?: return@characterCombatAttack
        target.setAnimation(animation)
        blocked = true
    }
}