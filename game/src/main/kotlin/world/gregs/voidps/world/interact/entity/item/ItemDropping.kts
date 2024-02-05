package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.interact.entity.player.equip.inventory
import world.gregs.voidps.world.interact.entity.sound.playSound

val floorItems: FloorItems by inject()
val logger = InlineLogger()

inventory({ inventory == "inventory" && option == "Drop" }) { player: Player ->
    player.queue.clearWeak()
    if (player.inventory.remove(slot, item.id, item.amount)) {
        if (item.tradeable) {
            floorItems.add(player.tile, item.id, item.amount, revealTicks = 100, disappearTicks = 200, owner = player)
        } else {
            floorItems.add(player.tile, item.id, item.amount, revealTicks = FloorItems.NEVER, disappearTicks = 300, owner = player)
        }
        player.playSound("drop_item")
    } else {
        logger.info { "Error dropping item $item for $player" }
    }
}