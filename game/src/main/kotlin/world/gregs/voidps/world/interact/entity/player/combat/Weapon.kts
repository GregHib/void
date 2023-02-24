package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.attackRange
import world.gregs.voidps.world.interact.entity.combat.weapon

on<Registered>(priority = Priority.HIGH) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index }, Priority.HIGH) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player.attackRange = weapon.def["attack_range", 1]
    player["attack_speed"] = weapon.def["attack_speed", 4]
    player.weapon = weapon
}