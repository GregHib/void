package content.entity.player.modal

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendInterfaceItemUpdate
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryUpdate

class Containers : Script {

    val inventoryDefinitions: InventoryDefinitions by inject()
    val itemDefs: ItemDefinitions by inject()

    init {
        inventoryUpdate { player ->
            val secondary = inventory.startsWith("_")
            val id = if (secondary) inventory.removePrefix("_") else inventory
            player.sendInterfaceItemUpdate(
                key = inventoryDefinitions.get(id).id,
                updates = updates.map { Triple(it.index, itemDefs.getOrNull(it.item.id)?.id ?: -1, it.item.amount) },
                secondary = secondary,
            )
        }
    }
}
