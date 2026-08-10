package content.entity.player.inv

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class ItemPacks : Script {
    init {
        itemOption("Open", "*_pack") { (item) ->
            val row = Rows.getOrNull("packs.${item.id}") ?: return@itemOption
            inventory.transaction {
                remove(item.id)
                add(row.item("unpacked"), row.int("amount"))
            }
        }

        itemOption("Open-All", "*_pack") { (item) ->
            val row = Rows.getOrNull("packs.${item.id}") ?: return@itemOption
            inventory.transaction {
                val removed = removeToLimit(item.id, item.amount)
                add(row.item("unpacked"), removed * row.int("amount"))
            }
            if (inventory.transaction.error is TransactionError.Full) {
                inventoryFull()
            }
        }
    }
}
