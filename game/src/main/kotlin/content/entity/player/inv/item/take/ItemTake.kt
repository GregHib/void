package content.entity.player.inv.item.take

import com.github.michaelbull.logging.InlineLogger
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.floorItemOperate
import world.gregs.voidps.engine.entity.item.floor.npcOperateFloorItem
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge
import world.gregs.voidps.engine.event.Script
@Script
class ItemTake {

    val floorItems: FloorItems by inject()
    val logger = InlineLogger()
    
    init {
        floorItemOperate("Take") {
            approachRange(-1)
            val takeable = Takeable(target.id)
            player.emit(takeable)
            if (takeable.cancelled) {
                return@floorItemOperate
            }
            val item = takeable.item
            if (player.inventory.isFull() && (!player.inventory.stackable(item) || !player.inventory.contains(item))) {
                player.inventoryFull()
                return@floorItemOperate
            }
            if (!floorItems.remove(target)) {
                player.message("Too late - it's gone!")
                return@floorItemOperate
            }
        
            player.inventory.transaction {
                val freeIndex = inventory.freeIndex()
                add(item, target.amount)
                if (target.charges > 0) {
                    setCharge(freeIndex, target.charges)
                }
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> {
                    if (player.tile != target.tile) {
                        player.face(target.tile.delta(player.tile))
                        player.anim("take")
                    }
                    player.sound("take_item")
                    player.emit(Taken(target, item))
                }
                is TransactionError.Full -> player.inventoryFull()
                else -> logger.warn { "Error taking item $target ${player.inventory.transaction.error}" }
            }
        }

        npcOperateFloorItem("Take") {
            if (!floorItems.remove(target)) {
                logger.warn { "$npc unable to take $target." }
            }
            if (npc.id == "ash_cleaner") {
                npc.anim("cleaner_sweeping")
                delay(2)
                npc.clearAnim()
            }
        }

    }

}
