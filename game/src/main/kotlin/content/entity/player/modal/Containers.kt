package content.entity.player.modal

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendInterfaceItemUpdate
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions

class Containers(val inventoryDefinitions: InventoryDefinitions) : Script {

    init {
        inventoryUpdated { inventory, updates ->
            val secondary = inventory.startsWith("_")
            val id = if (secondary) inventory.removePrefix("_") else inventory
            sendInterfaceItemUpdate(
                key = inventoryDefinitions.get(id).id,
                updates = updates.map { Triple(it.index, ItemDefinitions.getOrNull(it.item.id)?.id ?: -1, it.item.amount) },
                secondary = secondary,
            )
        }
    }
}
