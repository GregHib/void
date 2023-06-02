package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.contain.clear
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItemStorage
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound

val floorItems: FloorItemStorage by inject()
val logger = InlineLogger()

on<ContainerOption>({ container == "inventory" && option == "Drop" }) { player: Player ->
    player.queue.clearWeak()
    if (player.inventory.clear(slot) && item.isNotEmpty() && item.amount > 0) {
        if (item.tradeable) {
            floorItems.add(player.tile, item.id, item.amount, 100, 200, player)
        } else {
            floorItems.add(player.tile, item.id, item.amount, -1, 300, player)
        }
        player.playSound("drop_item")
    } else {
        logger.info { "Error dropping item $item for $player" }
    }
}