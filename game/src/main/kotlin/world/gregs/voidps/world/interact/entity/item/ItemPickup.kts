package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.turn
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.floorItemOperate
import world.gregs.voidps.engine.entity.item.floor.npcOperateFloorItem
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge
import world.gregs.voidps.world.interact.entity.sound.playSound

val floorItems: FloorItems by inject()
val logger = InlineLogger()

floorItemOperate("Take") {
    approachRange(-1)
    if (player.inventory.isFull() && (!player.inventory.stackable(target.id) || !player.inventory.contains(target.id))) {
        player.inventoryFull()
        return@floorItemOperate
    }
    if (!floorItems.remove(target)) {
        player.message("Too late - it's gone!")
        return@floorItemOperate
    }

    player.inventory.transaction {
        val freeIndex = inventory.freeIndex()
        add(target.id, target.amount)
        if (target.charges > 0) {
            setCharge(freeIndex, target.charges)
        }
    }
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

npcOperateFloorItem("Take") {
    if (!floorItems.remove(target)) {
        logger.warn { "$npc unable to pick up $target." }
    }
    if (npc.id == "ash_cleaner") {
        npc.setAnimation("cleaner_sweeping")
        delay(2)
        npc.clearAnimation()
    }
}