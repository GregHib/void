package content.entity.player.equip

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.encode.weight

class Weight : Script {

    init {
        playerSpawn(::updateWeight)

        inventoryUpdated("worn_equipment") { _, _ ->
            updateWeight(this)
        }

        inventoryUpdated("inventory") { _, _ ->
            updateWeight(this)
        }
    }

    fun Inventory.weight(): Double = items.sumOf { it.def["weight", 0.0] * it.amount }

    fun updateWeight(player: Player) {
        var weight = 0.0
        weight += player.equipment.weight()
        weight += player.inventory.weight()

        player["weight"] = weight
        player.client?.weight(weight.toInt())
    }
}
