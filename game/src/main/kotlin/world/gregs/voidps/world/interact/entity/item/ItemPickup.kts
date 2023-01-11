package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.onOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.remaining
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()
val logger = InlineLogger()

onOperate({ option == "Take" }) { player: Player, item: FloorItem ->
    if(player.remaining("last_movement") != -1L) {
        return@onOperate
    }
//    player.arriveDelay()
    if (player.inventory.isFull() && (!player.inventory.stackable(item.id) || !player.inventory.contains(item.id))) {
        player.inventoryFull()
    } else if (items.remove(item)) {
        player.inventory.add(item.id, item.amount)
        when (player.inventory.transaction.error) {
            TransactionError.None -> {
                if (!player.visuals.moved && player.tile != item.tile) {
                    player.face(item.tile.delta(player.tile).toDirection())
                    player.setAnimation("take")
                }
                player.playSound("pickup_item")
            }
            is TransactionError.Full -> player.inventoryFull()
            else -> logger.warn { "Error picking up item $item ${player.inventory.transaction.error}" }
        }
    }
}