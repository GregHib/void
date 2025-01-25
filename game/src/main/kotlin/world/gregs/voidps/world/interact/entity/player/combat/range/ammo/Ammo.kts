package world.gregs.voidps.world.interact.entity.player.combat.range.ammo

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasUseLevel
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.fightStyle
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.Ammo
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import content.entity.sound.playSound

val ammoDefinitions: AmmoDefinitions by inject()
val weaponStyles: WeaponStyleDefinitions by inject()

combatPrepare { player ->
    if (player.fightStyle != "range" && player.weapon.def["weapon_style", 0] == 21 && !checkAmmo(player)) { // Salamanders
        cancel()
    }
}

combatSwing(style = "blaze") { player ->
    if (player.weapon.def["weapon_style", 0] == 21) { // Salamanders
        Ammo.remove(player, target, player.ammo, Ammo.requiredAmount(player.weapon, false))
    }
}

combatSwing(style = "melee") { player ->
    if (player.weapon.def["weapon_style", 0] == 21) { // Salamanders
        Ammo.remove(player, target, player.ammo, Ammo.requiredAmount(player.weapon, false))
    }
}

combatPrepare("range") { player ->
    if (!checkAmmo(player)) {
        player.playSound("out_of_ammo")
        cancel()
    }
}

fun checkAmmo(player: Player): Boolean {
    player.ammo = ""
    if (!player.hasUseLevel(Skill.Ranged, player.weapon, message = true)) {
        player.specialAttack = false
        player.message("You are not high enough level to use this weapon.")
        player.message("You need to have a Ranged level of ${player.weapon.def.get<Int>("secondary_use_level")}.")
        return false
    }
    val style = weaponStyles.get(player.weapon.def["weapon_style", 0])
    val group = player.weapon.def["ammo_group", ""]
    val slot = when (style.stringId) {
        "pie", "chinchompa", "thrown" -> EquipSlot.Weapon
        "crossbow", "fixed_device", "salamander" -> EquipSlot.Ammo
        "bow" -> if (group == "none") EquipSlot.Weapon else EquipSlot.Ammo
        else -> EquipSlot.Weapon
    }
    if (slot == EquipSlot.Weapon) {
        val weapon = player.equipped(EquipSlot.Weapon)
        when {
            weapon.id == "zaryte_bow" -> player.ammo = "zaryte_arrow"
            weapon.id.endsWith("sling") -> player.ammo = "sling_rock"
            weapon.id.startsWith("crystal_bow") -> player.ammo = "special_arrow"
            else -> player.ammo = weapon.id
        }
        return true
    }
    val ammo = player.equipped(slot)
    if (!player.hasUseLevel(Skill.Ranged, ammo)) {
        player.message("You are not high enough level to use this item.")
        player.message("You need to have a Ranged level of ${ammo.def.get<Int>("secondary_use_level")}.")
        return false
    }
    val required = Ammo.requiredAmount(player.weapon, player.specialAttack)
    if (ammo.amount < required) {
        player.message("There is no ammo left in your quiver.")
        return false
    }
    val definition = ammoDefinitions.get(group)
    if (!definition.items.contains(ammo.id)) {
        player.message("You can't use that ammo with your ${style.stringId.toLowerSpaceCase()}.")
        return false
    }
    // Ammo is kept track of as EquipSlot.Ammo could've been used up
    if (ammo.id == "barbed_bolts" || ammo.id == "bone_bolts") {
        player.ammo = ammo.id
    } else if (group == "crossbow") {
        player.ammo = "crossbow_bolt"
    } else if (ammo.id.endsWith("fire_arrows_lit")) {
        player.ammo = "fire_arrows_lit"
    } else if (ammo.id.endsWith("fire_arrows_unlit")) {
        player.ammo = "fire_arrows_unlit"
    } else {
        player.ammo = ammo.id
    }
    return true
}