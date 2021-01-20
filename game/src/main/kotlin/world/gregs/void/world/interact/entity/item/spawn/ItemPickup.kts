import kotlinx.coroutines.cancel
import world.gregs.void.engine.entity.Unregistered
import world.gregs.void.engine.entity.character.contain.ContainerResult
import world.gregs.void.engine.entity.character.contain.inventory
import world.gregs.void.engine.entity.item.FloorItemOption
import world.gregs.void.engine.entity.item.FloorItemState
import world.gregs.void.engine.entity.item.FloorItems
import world.gregs.void.engine.entity.item.offset
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.engine.map.chunk.ChunkBatcher
import world.gregs.void.network.codec.game.encode.FloorItemRemoveEncoder
import world.gregs.void.network.codec.game.encode.message
import world.gregs.void.utility.inject

val items: FloorItems by inject()
val batcher: ChunkBatcher by inject()
val bus: EventBus by inject()
val removeEncoder: FloorItemRemoveEncoder by inject()

FloorItemOption where { option == "Take" } then {
    val item = floorItem
    item.disappear?.cancel("Floor item picked up.")
    val result = player.inventory.add(item.id, item.amount)
    if(result) {
        item.state = FloorItemState.Removed
        batcher.update(item.tile.chunk) { player -> removeEncoder.encode(player, item.tile.offset(), item.id) }
        items.remove(item)
        bus.emit(Unregistered(item))
    } else {
        when(player.inventory.result) {
            ContainerResult.Full, ContainerResult.Overflow -> {
                player.message("Your inventory is full.")
            }
            else -> println("Failure ${player.inventory.result}")
        }
        // TODO
    }
}