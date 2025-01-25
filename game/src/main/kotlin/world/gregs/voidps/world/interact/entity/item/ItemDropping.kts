package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption
import content.entity.sound.playSound

val floorItems: FloorItems by inject()
val logger = InlineLogger()

inventoryOption("Drop", "inventory") {
    player.queue.clearWeak()
    val event = Droppable(item)
    player.emit(event)
    if (event.cancelled) {
        return@inventoryOption
    }
    if (player.inventory.remove(slot, item.id, item.amount)) {
        if (item.tradeable) {
            floorItems.add(player.tile, item.id, item.amount, revealTicks = 100, disappearTicks = 200, owner = player)
        } else {
            floorItems.add(player.tile, item.id, item.amount, charges = item.charges(), revealTicks = FloorItems.NEVER, disappearTicks = 300, owner = player)
        }
        player.emit(Dropped(item))
        player.playSound("drop_item")
    } else {
        logger.info { "Error dropping item $item for $player" }
    }
}