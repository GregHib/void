package world.gregs.voidps.world.interact.entity.player.combat.range.ammo

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasUseLevel
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.Ammo
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

val ammoDefinitions: AmmoDefinitions by inject()

combatSwing(style = "range", override = false) { player ->
    check(player)
}

combatSwing(style = "range", override = false) { player ->
    check(player)
}

fun CombatSwing.check(player: Player) {
    if (!player.hasUseLevel(Skill.Ranged, player.weapon, message = true)) {
        delay = -1
        player.specialAttack = false
        player.message("You are not high enough level to use this weapon.")
        player.message("You need to have a Ranged level of ${player.weapon.def.get<Int>("secondary_use_level")}.")
        return
    }
}

val handler: suspend CombatSwing.(Player) -> Unit = handler@{ player ->
    if (!Ammo.required(player.weapon)) {
        return@handler
    }
    val required = if (player.specialAttack && player.weapon.id.startsWith("magic_shortbow")) 2 else player.weapon.def["ammo_required", 1]
    val ammo = player.equipped(EquipSlot.Ammo)
    player.ammo = ""
    if (ammo.amount < required) {
        player.message("There is no ammo left in your quiver.")
        delay = -1
        return@handler
    }
    if (!player.hasUseLevel(Skill.Ranged, ammo)) {
        player.message("You are not high enough level to use this item.")
        player.message("You need to have a Ranged level of ${ammo.def.get<Int>("secondary_use_level")}.")
        delay = -1
        return@handler
    }
    val weapon = player.weapon
    val group = weapon.def["ammo_group", ""]
    if (!ammoDefinitions.get(group).items.contains(ammo.id)) {
        player.message("You can't use that ammo with your bow.")
        delay = -1
        return@handler
    }

    Ammo.remove(player, target, ammo.id, required)

    // Ammo is kept track of as EquipSlot.Ammo could've been used up
    player.ammo = when {
        ammo.id.endsWith("fire_arrows_lit") -> "fire_arrows_lit"
        ammo.id.endsWith("fire_arrows_unlit") -> "fire_arrows_unlit"
        else -> ammo.id
    }
}
combatSwing("*bow", "range", block = handler)
combatSwing("seercull", "range", block = handler)
combatSwing("*longbow_sighted", "range", block = handler)

combatSwing("zaryte_bow", "range", override = false) { player ->
    player.ammo = "zaryte_arrow"
}

combatSwing("*sling", "range", override = false) { player ->
    player.ammo = "sling_rock"
}

combatSwing("*chinchompa", "range", override = false) { player ->
    player.ammo = player.weapon.id
}

combatSwing("crystal_bow*", "range", override = false) { player ->
    player.ammo = "special_arrow"
}