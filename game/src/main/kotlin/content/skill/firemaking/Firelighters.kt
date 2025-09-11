package content.skill.firemaking

import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

@Script
class Firelighters {

    init {
        itemOnItem("logs", "*firelighter") { player ->
            player.inventory.transaction {
                remove(fromItem.id)
                val colour = fromItem.id.removeSuffix("_firelighter")
                replace(toItem.id, "${colour}_logs")
            }
        }
    }
}
