package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import content.entity.player.dialogue.type.destroy
import content.entity.player.inv.inventoryOptions
import content.entity.sound.playSound

val logger = InlineLogger()

inventoryOptions("Destroy", "Dismiss", "Release", inventory = "inventory") {
    if (item.isEmpty() || item.amount <= 0) {
        logger.info { "Error destroying item $item for $player" }
        return@inventoryOptions
    }
    val message = item.def["destroy", """
        Are you sure you want to ${option.lowercase()} ${item.def.name}?
        You won't be able to reclaim it.
    """]
    val destroy = destroy(item.id, message)
    if (!destroy) {
        return@inventoryOptions
    }
    val event = Destructible(item)
    player.emit(event)
    if (event.cancelled) {
        return@inventoryOptions
    }
    if (player.inventory.remove(slot, item.id, item.amount)) {
        player.playSound("destroy_object")
        player.emit(Destroyed(item))
        logger.info { "$player destroyed item $item" }
    } else {
        logger.info { "Error destroying item $item for $player" }
    }
}