package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inv.itemChanged
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.attackRange
import world.gregs.voidps.world.interact.entity.combat.weapon

playerSpawn(priority = Priority.HIGH) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

itemChanged({ inventory == "worn_equipment" && index == EquipSlot.Weapon.index }, Priority.HIGH) { player: Player ->
    updateWeapon(player, item)
}

variableSet({ key == "autocast" && to == null }) { player: Player ->
    updateWeapon(player, player.weapon)
}

variableSet({ key == "spell" && to == null }) { player: Player ->
    updateWeapon(player, player.weapon)
}

variableSet({ key == "attack_style" && to == "long_range" }) { player: Player ->
    updateWeapon(player, player.weapon, 2)
}

variableSet({ key == "attack_style" && from == "long_range" && to != "long_range" }) { player: Player ->
    updateWeapon(player, player.weapon)
}

fun updateWeapon(player: Player, weapon: Item, range: Int = 0) {
    player.attackRange = (weapon.def["attack_range", 1] + range).coerceAtMost(10)
    player["attack_speed"] = weapon.def["attack_speed", 4]
    player.weapon = weapon
}