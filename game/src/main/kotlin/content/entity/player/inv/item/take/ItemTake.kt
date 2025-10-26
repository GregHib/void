package content.entity.player.inv.item.take

import com.github.michaelbull.logging.InlineLogger
import content.entity.sound.sound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.Operate
import world.gregs.voidps.engine.entity.character.mode.interact.approachRange
import world.gregs.voidps.engine.entity.character.mode.interact.delay
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge

@Script
class ItemTake : Api {

    val floorItems: FloorItems by inject()
    val logger = InlineLogger()

    @Operate("Take")
    override suspend fun operate(player: Player, target: FloorItem, option: String) {
        player.approachRange(-1)
        val takeable = Takeable(target.id)
        player.emit(takeable)
        if (takeable.cancelled) {
            return
        }
        val item = takeable.item
        if (player.inventory.isFull() && (!player.inventory.stackable(item) || !player.inventory.contains(item))) {
            player.inventoryFull()
            return
        }
        if (!floorItems.remove(target)) {
            player.message("Too late - it's gone!")
            return
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
                AuditLog.event(player, "took", target, target.tile)
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

    @Operate("Take")
    override suspend fun operate(npc: NPC, target: FloorItem, option: String) {
        if (!floorItems.remove(target)) {
            logger.warn { "$npc unable to take $target." }
        }
        if (npc.id == "ash_cleaner") {
            npc.anim("cleaner_sweeping")
            npc.delay(2)
            npc.clearAnim()
        }
    }
}
