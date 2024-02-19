package world.gregs.voidps.world.interact.entity.player.combat.range.ammo

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasUseLevel
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.range.Ammo
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

val ammoDefinitions: AmmoDefinitions by inject()

weaponSwing(style = "range", priority = Priority.HIGHEST) { player ->
    if (!player.hasUseLevel(Skill.Ranged, player.weapon, message = true)) {
        delay = -1
        player.specialAttack = false
        player.message("You are not high enough level to use this weapon.")
        player.message("You need to have a Ranged level of ${player.weapon.def.get<Int>("secondary_use_level")}.")
        return@weaponSwing
    }
}

weaponSwing("*bow", "seercull", "*longbow_sighted", style = "range", priority = Priority.HIGHEST) { player ->
    if (!Ammo.required(player.weapon)) {
        return@weaponSwing
    }
    player["required_ammo"] = player.weapon.def["ammo_required", 1]
}

weaponSwing("*bow", "seercull", "*longbow_sighted", style = "range", priority = Priority.HIGH) { player ->
    if (!Ammo.required(player.weapon)) {
        return@weaponSwing
    }
    val required = player["required_ammo", 1]
    val ammo = player.equipped(EquipSlot.Ammo)
    player.ammo = ""
    if (ammo.amount < required) {
        player.message("There is no ammo left in your quiver.")
        delay = -1
        return@weaponSwing
    }
    if (!player.hasUseLevel(Skill.Ranged, ammo)) {
        player.message("You are not high enough level to use this item.")
        player.message("You need to have a Ranged level of ${ammo.def.get<Int>("secondary_use_level")}.")
        delay = -1
        return@weaponSwing
    }
    val weapon = player.weapon
    val group = weapon.def["ammo_group", ""]
    if (!ammoDefinitions.get(group).items.contains(ammo.id)) {
        player.message("You can't use that ammo with your bow.")
        delay = -1
        return@weaponSwing
    }

    Ammo.remove(player, target, ammo.id, required)

    // Ammo is kept track of as EquipSlot.Ammo could've been used up
    player.ammo = when {
        ammo.id.endsWith("fire_arrows_lit") -> "fire_arrows_lit"
        ammo.id.endsWith("fire_arrows_unlit") -> "fire_arrows_unlit"
        else -> ammo.id
    }
}

weaponSwing("*", style = "range", priority = Priority.HIGH) { player ->
    if (Ammo.required(player.weapon)) {
        return@weaponSwing
    }
    player.ammo = when {
        player.weapon.id == "zaryte_bow" -> "zaryte_arrow"
        player.weapon.id.endsWith("sling") -> "sling_rock"
        player.weapon.id.endsWith("chinchompa") -> player.weapon.id
        player.weapon.id.startsWith("crystal_bow") -> "special_arrow"
        else -> return@weaponSwing
    }
}