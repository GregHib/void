package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.turn
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.world.interact.entity.sound.playSound

val floorItems: FloorItems by inject()
val logger = InlineLogger()

on<FloorItemOption>({ operate && option == "Take" }) { player: Player ->
    arriveDelay()
    player.approachRange(-1)
    if (player.inventory.isFull() && (!player.inventory.stackable(target.id) || !player.inventory.contains(target.id))) {
        player.inventoryFull()
        return@on
    }
    if (!floorItems.remove(target)) {
        logger.warn { "$player unable to pick up $target." }
        return@on
    }
    player.inventory.add(target.id, target.amount)
    when (player.inventory.transaction.error) {
        TransactionError.None -> {
            if (player.tile != target.tile) {
                player.turn(target.tile.delta(player.tile))
                player.setAnimation("take")
            }
            player.playSound("pickup_item")
        }
        is TransactionError.Full -> player.inventoryFull()
        else -> logger.warn { "Error picking up item $target ${player.inventory.transaction.error}" }
    }
}

on<FloorItemOption>({ operate && option == "Take" }) { npc: NPC ->
    if (!floorItems.remove(target)) {
        logger.warn { "$npc unable to pick up $target." }
    }
}