package content.bot.interact.item

import content.bot.Bot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.network.client.instruction.InteractFloorItem

suspend fun Bot.pickup(floorItem: FloorItem) {
    player.instructions.send(InteractFloorItem(floorItem.def.id, floorItem.tile.x, floorItem.tile.y, 2))
    if (player.inventory.isFull()) {
        return
    }
    withTimeoutOrNull(TICKS.toMillis(2)) {
        suspendCancellableCoroutine<Unit> { cont ->
            this@pickup["floor_item_job"] = cont
            this@pickup["floor_item_hash"] = floorItem.hashCode()
        }
    }
}
