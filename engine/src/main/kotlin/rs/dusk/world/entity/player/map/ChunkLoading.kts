import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerRegistered
import rs.dusk.engine.model.entity.index.player.PlayerUnregistered
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.map.chunk.ChunkBatcher
import rs.dusk.engine.model.world.view
import rs.dusk.utility.func.nearby
import rs.dusk.utility.inject
import rs.dusk.world.entity.player.map.RegionLoaded


/**
 * Keeps track of local chunks for batched updates
 */
val batcher: ChunkBatcher by inject()

PlayerRegistered then {
    load(player)
}

PlayerUnregistered then {
    clearAll(player)
}

Moved where { entity is Player && from.chunk != to.chunk && entity.viewport.loaded } then {
    val player = entity as Player
    val lastView = player.movement.lastTile.chunkPlane.view(viewDistance)
    val view = player.tile.chunkPlane.view(viewDistance)
    // TODO what if plane changed?
    for(chunkPlane in view) {
        if(lastView.contains(chunkPlane)) {
            continue
        }
        batcher.sendCreation(player, chunkPlane)
    }
}

val viewDistance = 3

fun clearAll(player: Player) {
    val chunk = player.tile.chunk
    for(x in chunk.x.nearby(viewDistance)) {
        for (y in chunk.y.nearby(viewDistance)) {
            val chunkPlane = ChunkPlane(x, y, player.tile.plane)
            batcher.unsubscribe(player, chunkPlane)
        }
    }
}

fun load(player: Player) {
    val chunk = player.tile.chunk
    for(x in chunk.x.nearby(viewDistance)) {
        for(y in chunk.y.nearby(viewDistance)) {
            val chunkPlane = ChunkPlane(x, y, player.tile.plane)
            batcher.subscribe(player, chunkPlane)
        }
    }
}

RegionLoaded then {
    val lastView = player.movement.lastTile.region.view(viewDistance)
    val loadChunk = player.viewport.lastLoadChunk
    val view = loadChunk.view(player.viewport.size shr 4)
    // TODO what about loading a new plane on login when no movement
    for(chunk in view) {
        if(lastView.contains(chunk.region)) {
            continue
        }
        batcher.sendCreation(player, ChunkPlane(chunk.x, chunk.y, player.tile.plane))
    }
}