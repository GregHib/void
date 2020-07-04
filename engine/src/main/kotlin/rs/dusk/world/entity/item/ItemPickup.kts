import kotlinx.coroutines.cancel
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.index.contain.ContainerResult
import rs.dusk.engine.model.entity.index.contain.inventory
import rs.dusk.engine.model.entity.item.FloorItemOption
import rs.dusk.engine.model.entity.item.FloorItemState
import rs.dusk.engine.model.entity.item.FloorItems
import rs.dusk.engine.model.entity.item.offset
import rs.dusk.engine.model.world.map.chunk.ChunkBatcher
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRemoveMessage
import rs.dusk.utility.inject

val items: FloorItems by inject()
val batcher: ChunkBatcher by inject()
val bus: EventBus by inject()

FloorItemOption where { option == "Take" } then {
    val item = floorItem
    item.disappear?.cancel("Floor item picked up.")
    val result = player.inventory.add(item.id, item.amount)
    if(result is ContainerResult.Addition.Added) {
        item.state = FloorItemState.Removed
        batcher.update(item.tile.chunkPlane, FloorItemRemoveMessage(item.tile.offset(), item.id))
        items.remove(item)
        bus.emit(Unregistered(item))
    } else if(result is ContainerResult.Addition.Failure) {
        println("Failure $result")
        // TODO
    }
}