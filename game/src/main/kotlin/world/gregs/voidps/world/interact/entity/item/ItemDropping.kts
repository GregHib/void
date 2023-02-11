package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.contain.clear
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()
val logger = InlineLogger()

on<ContainerOption>({ container == "inventory" && option == "Drop" }) { player: Player ->
    if (player.inventory.clear(slot) && item.isNotEmpty() && item.amount > 0) {
        if (item.tradeable) {
            items.add(item.id, item.amount, player.tile, 100, 200, player)
        } else {
            items.add(item.id, item.amount, player.tile, -1, 300, player)
        }
        player.playSound("drop_item")
        player.queue.clearWeak()
    } else {
        logger.info { "Error dropping item $item for $player" }
    }
}