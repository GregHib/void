package content.entity.player.inv.item.drop

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.inv.item.tradeable
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ItemDropping : Script {

    val floorItems: FloorItems by inject()
    val logger = InlineLogger()

    init {
        itemOption("Drop") { (item, slot) ->
            queue.clearWeak()
            val event = Droppable(item, tile)
            emit(event)
            if (event.cancelled) {
                return@itemOption
            }
            AuditLog.event(this, "dropped", item, tile)
            if (inventory.remove(slot, item.id, item.amount)) {
                if (item.tradeable) {
                    floorItems.add(tile, item.id, item.amount, revealTicks = 100, disappearTicks = 200, owner = this)
                } else {
                    floorItems.add(tile, item.id, item.amount, charges = item.charges(), revealTicks = FloorItems.NEVER, disappearTicks = 300, owner = this)
                }
                emit(Dropped(item))
                sound("drop_item")
            } else {
                logger.info { "Error dropping item $item for $this" }
            }
        }
    }
}
