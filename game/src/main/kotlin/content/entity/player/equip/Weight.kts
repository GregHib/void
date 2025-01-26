package content.entity.player.equip

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.itemChange
import world.gregs.voidps.network.login.protocol.encode.weight

itemChange("worn_equipment", "inventory") { player ->
    updateWeight(player)
}

playerSpawn { player ->
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