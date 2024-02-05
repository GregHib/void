package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.itemChanged
import world.gregs.voidps.network.encode.weight

itemChanged({ inventory == "worn_equipment" || inventory == "inventory" }) { player: Player ->
    updateWeight(player)
}

playerSpawn { player: Player ->
    updateWeight(player)
}

fun Inventory.weight(): Double = items.sumOf { it.def["weight", 0.0] * it.amount }

fun updateWeight(player: Player) {
    var weight = 0.0
    weight += player.equipment.weight()
    weight += player.inventory.weight()

    player["weight"] = weight
    player.client?.weight(weight.toInt())
}