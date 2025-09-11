package content.entity.player.inv.item.destroy

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.destroy
import content.entity.player.inv.inventoryOptions
import content.entity.sound.sound
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

@Script
class ItemDestroy {

    val logger = InlineLogger()

    init {
        inventoryOptions("Destroy", "Dismiss", "Release", inventory = "inventory") {
            if (item.isEmpty() || item.amount <= 0) {
                logger.info { "Error destroying item $item for $player" }
                return@inventoryOptions
            }
            val message = item.def[
                "destroy", """
                Are you sure you want to ${option.lowercase()} ${item.def.name}?
                You won't be able to reclaim it.
            """,
            ]
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
                player.sound("destroy_object")
                player.emit(Destroyed(item))
                logger.info { "$player destroyed item $item" }
            } else {
                logger.info { "Error destroying item $item for $player" }
            }
        }
    }
}
