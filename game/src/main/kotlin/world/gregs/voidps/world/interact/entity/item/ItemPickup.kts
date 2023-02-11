package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.turn
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()
val logger = InlineLogger()

on<FloorItemOption>({ option == "Take" }) { player: Player ->
    if (player.inventory.isFull() && (!player.inventory.stackable(item.id) || !player.inventory.contains(item.id))) {
        player.inventoryFull()
    } else if (items.remove(item)) {
        player.inventory.add(item.id, item.amount)
        when (player.inventory.transaction.error) {
            TransactionError.None -> {
                if (!player.visuals.moved && player.tile != item.tile) {
                    player.turn(item.tile.delta(player.tile))
                    player.setAnimation("take")
                }
                player.playSound("pickup_item")
            }
            is TransactionError.Full -> player.inventoryFull()
            else -> logger.warn { "Error picking up item $item ${player.inventory.transaction.error}" }
        }
    }
}