import world.gregs.void.engine.entity.character.move.PlayerMoved
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerRegistered
import world.gregs.void.engine.entity.character.player.PlayerUnregistered
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.area.area
import world.gregs.void.engine.map.chunk.Chunk
import world.gregs.void.engine.map.chunk.ChunkBatcher
import world.gregs.void.utility.inject


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

PlayerMoved where { from.chunk != to.chunk } then {
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
    val view = tile.chunk.area(player.viewport.tileSize shr 5, planes = 4)
    for (chunk in view) {
        block(chunk)
    }
}