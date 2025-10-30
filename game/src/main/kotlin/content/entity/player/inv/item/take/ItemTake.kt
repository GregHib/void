package content.entity.player.inv.item.take

import com.github.michaelbull.logging.InlineLogger
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge

class ItemTake : Script {

    val floorItems: FloorItems by inject()
    val logger = InlineLogger()

    init {
        floorItemOperate("Take") { (target) ->
            approachRange(-1)
            val takeable = Takeable(target.id)
            emit(takeable)
            if (takeable.cancelled) {
                return@floorItemOperate
            }
            val item = takeable.item
            if (inventory.isFull() && (!inventory.stackable(item) || !inventory.contains(item))) {
                inventoryFull()
                return@floorItemOperate
            }
            if (!floorItems.remove(target)) {
                message("Too late - it's gone!")
                return@floorItemOperate
            }

            inventory.transaction {
                val freeIndex = inventory.freeIndex()
                add(item, target.amount)
                if (target.charges > 0) {
                    setCharge(freeIndex, target.charges)
                }
            }
            when (inventory.transaction.error) {
                TransactionError.None -> {
                    AuditLog.event(this, "took", target, target.tile)
                    if (tile != target.tile) {
                        face(target.tile.delta(tile))
                        anim("take")
                    }
                    sound("take_item")
                    emit(Taken(target, item))
                }
                is TransactionError.Full -> inventoryFull()
                else -> logger.warn { "Error taking item $target ${inventory.transaction.error}" }
            }
        }

        npcOperateFloorItem("Take") { (target) ->
            if (!floorItems.remove(target)) {
                logger.warn { "$this unable to take $target." }
            }
            if (id == "ash_cleaner") {
                anim("cleaner_sweeping")
                delay(2)
                clearAnim()
            }
        }
    }
}
