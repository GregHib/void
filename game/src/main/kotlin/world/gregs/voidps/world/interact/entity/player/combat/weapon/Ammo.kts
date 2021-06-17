package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.ammo
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isBowOrCrossbow(item: Item) = item.name.endsWith("bow")

fun ammoRequired(item: Item) = !item.name.startsWith("crystal_bow") && item.name != "zaryte_bow"

on<CombatSwing>({ player -> isBowOrCrossbow(player.weapon) && ammoRequired(player.weapon) }, Priority.HIGHEST) { player: Player ->
    player["required_ammo"] = 1
}

on<CombatSwing>({ player -> isBowOrCrossbow(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.equipped(EquipSlot.Ammo)
    if (!player.equipment.remove(ammo.name, required)) {
        player.message("There is no ammo left in your quiver.")
        player.action.cancel(ActionType.Combat)
        return@on
    }

    val weapon = player.weapon
    if (weapon.def.ammo?.contains(ammo.name) != true) {
        player.message("You can't use that ammo with your bow.")
        player.action.cancel(ActionType.Combat)
        return@on
    }
}