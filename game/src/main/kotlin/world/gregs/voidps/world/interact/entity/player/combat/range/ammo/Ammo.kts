package world.gregs.voidps.world.interact.entity.player.combat.range.ammo

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasUseLevel
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.Weapon
import world.gregs.voidps.world.interact.entity.combat.fightStyle
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.Ammo
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

on<CombatSwing>({ player -> player.fightStyle == "range" }, Priority.HIGHEST) { player: Player ->
    if (!player.hasUseLevel(Skill.Ranged, player.weapon, message = true)) {
        delay = -1
        player.specialAttack = false
        player.message("You are not high enough level to use this weapon.")
        player.message("You need to have a Ranged level of ${player.weapon.def.get<Int>("secondary_use_level")}.")
        return@on
    }
}

on<CombatSwing>({ player -> player.fightStyle == "range" && Weapon.isBowOrCrossbow(player.weapon) && Ammo.required(player.weapon) }, Priority.HIGHEST) { player: Player ->
    player["required_ammo"] = player.weapon.def["ammo_required", 1]
}

on<CombatSwing>({ player -> player.fightStyle == "range" && Weapon.isBowOrCrossbow(player.weapon) && Ammo.required(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.equipped(EquipSlot.Ammo)
    player.ammo = ""
    if (!player.hasUseLevel(Skill.Ranged, ammo)) {
        player.message("You are not high enough level to use this item.")
        player.message("You need to have a Ranged level of ${ammo.def.get<Int>("secondary_use_level")}.")
        delay = -1
        return@on
    }
    if (ammo.amount < required) {
        player.message("There is no ammo left in your quiver.")
        delay = -1
        return@on
    }

    val weapon = player.weapon
    if (!weapon.def.ammo.contains(ammo.id)) {
        player.message("You can't use that ammo with your bow.")
        delay = -1
        return@on
    }

    Ammo.remove(player, target, ammo.id, required)

    // Ammo is kept track of as EquipSlot.Ammo could've been used up
    player.ammo = when {
        ammo.id.endsWith("fire_arrows_lit") -> "fire_arrows_lit"
        ammo.id.endsWith("fire_arrows_unlit") -> "fire_arrows_unlit"
        else -> ammo.id
    }
}

on<CombatSwing>({ player -> player.fightStyle == "range" && !Ammo.required(player.weapon) }, Priority.HIGH) { player: Player ->
    player.ammo = when {
        player.weapon.id == "zaryte_bow" -> "zaryte_arrow"
        player.weapon.id.endsWith("sling") -> "sling_rock"
        player.weapon.id.endsWith("chinchompa") -> player.weapon.id
        player.weapon.id.startsWith("crystal_bow") -> "special_arrow"
        else -> return@on
    }
}