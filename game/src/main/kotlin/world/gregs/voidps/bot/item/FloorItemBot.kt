package world.gregs.voidps.bot.item

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.network.instruct.InteractFloorItem
import kotlin.coroutines.resume

suspend fun Bot.pickup(floorItem: FloorItem) {
    player.instructions.emit(InteractFloorItem(floorItem.def.id, floorItem.tile.x, floorItem.tile.y, 2))
    suspendCancellableCoroutine<Unit> { cont ->
        floorItem.events.on<FloorItem, Unregistered> {
            cont.resume(Unit)
        }
    }
}