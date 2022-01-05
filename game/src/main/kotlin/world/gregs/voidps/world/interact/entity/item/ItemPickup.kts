package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.contain.ContainerResult
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()
val logger = InlineLogger()

on<FloorItemOption>({ option == "Take" }) { player: Player ->
    val item = floorItem
    if (player.inventory.isFull() && (!player.inventory.stackable(item.id) || !player.inventory.contains(item.id))) {
        player.inventoryFull()
    } else if (items.remove(item)) {
        player.playSound("pickup_item")
        if (!player.inventory.add(item.id, item.amount)) {
            when (player.inventory.result) {
                ContainerResult.Full, ContainerResult.Overflow -> player.inventoryFull()
                else -> logger.warn { "Error picking up item $item ${player.inventory.result}" }
            }
        }
    }
}