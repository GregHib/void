package content.entity.player.inv.item.take

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge

class ItemTake : Script {

    val logger = InlineLogger()

    init {
        floorItemOperate("Take") { (target) ->
            arriveDelay()
            approachRange(-1)
            if (!take(this, target)) {
                return@floorItemOperate
            }
            AuditLog.event(this, "took", target, target.tile)
            if (tile != target.tile) {
                face(target.tile.delta(tile))
                anim("take")
            }
            Items.take(this, target)
        }

        npcOperateFloorItem("Take") { (target) ->
            arriveDelay()
            if (!FloorItems.remove(target)) {
                logger.warn { "$this unable to take $target." }
            }
            if (id == "ash_cleaner") {
                anim("cleaner_sweeping")
                delay(2)
                clearAnim()
            }
        }
    }

    companion object {
        private val logger = InlineLogger()
        fun take(player: Player, target: FloorItem): Boolean {
            val item = Items.takeable(player, target.id) ?: return false
            if (player.inventory.isFull() && (!player.inventory.stackable(item) || !player.inventory.contains(item))) {
                player.inventoryFull()
                return false
            }
            if (!FloorItems.remove(target)) {
                player.message("Too late - it's gone!")
                return false
            }
            player.inventory.transaction {
                val index = add(item, target.amount)
                if (target.charges > 0 && index != -1) {
                    setCharge(index, target.charges)
                }
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> {
                    player.sound("take_item")
                    return true
                }
                is TransactionError.Full -> player.inventoryFull()
                else -> logger.warn { "Error taking item $target ${player.inventory.transaction.error}" }
            }
            return false
        }
    }
}
