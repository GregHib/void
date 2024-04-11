package world.gregs.voidps.world.activity.skill.firemaking

import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.World.remove
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.inv.transact.remove

itemOnItem("logs", "*firelighter") { player ->
    player.inventory.transaction {
        remove(fromItem.id)
        val colour = fromItem.id.removeSuffix("_firelighter")
        replace(toItem.id, "${colour}_logs")
    }
}