import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.utility.inject

/**
 * Keeps track of local chunks for batched updates
 */
val batches: ChunkBatches by inject()

on<Registered> { player: Player ->
    load(player)
}

on<Unregistered> { player: Player ->
    forEachChunk(player, player.tile) { chunk ->
        batches.unsubscribe(player, chunk)
    }
}

on<Moved>({ from.chunk != to.chunk }) { player: Player ->
    forEachChunk(player, from) { chunk ->
        batches.unsubscribe(player, chunk)
    }
    load(player)
}

fun load(player: Player) {
    forEachChunk(player, player.tile) { chunk ->
        if(batches.subscribe(player, chunk)) {
            batches.sendInitial(player, chunk)
        }
    }
}

fun forEachChunk(player: Player, tile: Tile, block: (Chunk) -> Unit) {
    val view = tile.chunk.toCuboid(radius = player.viewport.tileSize shr 5).copy(minPlane = 0, maxPlane = 3).toChunks()
    for (chunk in view) {
        block(chunk)
    }
}