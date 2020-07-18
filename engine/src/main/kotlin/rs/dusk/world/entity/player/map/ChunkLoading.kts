import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerRegistered
import rs.dusk.engine.model.entity.index.player.PlayerUnregistered
import rs.dusk.engine.model.world.Chunk
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
    forEachChunk(player, player.tile) { chunk ->
        batcher.unsubscribe(player, chunk)
    }
}

Moved where { entity is Player && from.chunk != to.chunk } then {
    val player = entity as Player
    forEachChunk(player, from) { chunk ->
        batcher.unsubscribe(player, chunk)
    }
    load(player)
}

fun load(player: Player) {
    forEachChunk(player, player.tile) { chunk ->
        if(batcher.subscribe(player, chunk)) {
            batcher.sendInitial(player, chunk)
        }
    }
}

fun forEachChunk(player: Player, tile: Tile, block: (Chunk) -> Unit) {
    val view = tile.chunk.view(player.viewport.size shr 5)
    for (chunk in view) {
        block(chunk)
    }
}