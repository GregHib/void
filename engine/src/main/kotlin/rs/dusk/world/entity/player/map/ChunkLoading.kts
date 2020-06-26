import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerRegistered
import rs.dusk.engine.model.entity.index.player.PlayerUnregistered
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.chunk.ChunkBatcher
import rs.dusk.engine.model.world.view
import rs.dusk.utility.inject


/**
 * Keeps track of local chunks for batched updates
 */
val batcher: ChunkBatcher by inject()

PlayerRegistered then {
    load(player)
}

PlayerUnregistered then {
    forEachChunk(player, player.tile) { chunkPlane ->
        batcher.unsubscribe(player, chunkPlane)
    }
}

Moved where { entity is Player && from.chunkPlane != to.chunkPlane } then {
    val player = entity as Player
    forEachChunk(player, player.movement.lastTile) { chunkPlane ->
        batcher.unsubscribe(player, chunkPlane)
    }
    load(player)
}

fun load(player: Player) {
    forEachChunk(player, player.tile) { chunkPlane ->
        if(batcher.subscribe(player, chunkPlane)) {
            batcher.sendInitial(player, chunkPlane)
        }
    }
}

fun forEachChunk(player: Player, tile: Tile, block: (ChunkPlane) -> Unit) {
    val view = tile.chunkPlane.view(player.viewport.size shr 5)
    for (chunkPlane in view) {
        block(chunkPlane)
    }
}