import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Moving
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.utility.inject

/**
 * Keeps track of local chunks for batched updates
 */
val batches: ChunkBatches by inject()

on<Registered> { player: Player ->
    forEachChunk(player, player.tile) { chunk ->
        if (batches.subscribe(player, chunk)) {
            batches.sendInitial(player, chunk)
        }
    }
}

on<Unregistered> { player: Player ->
    forEachChunk(player, player.tile) { chunk ->
        batches.unsubscribe(player, chunk)
    }
}

on<Moving>({ from.chunk != to.chunk || from.plane != to.plane }) { player: Player ->
    val radius = player.viewport.tileSize shr 5
    val fromArea = from.chunk.toRectangle(radius)
    val toArea = to.chunk.toRectangle(radius)
    val changedPlane = from.plane != to.plane
    forEachChunk(player, from) { chunk: Chunk ->
        if (changedPlane || !chunk.toRectangle().intersects(toArea)) {
            batches.unsubscribe(player, chunk)
        }
    }
    forEachChunk(player, to) { chunk: Chunk ->
        if ((changedPlane || !chunk.toRectangle().intersects(fromArea)) && batches.subscribe(player, chunk)) {
            batches.sendInitial(player, chunk)
        }
    }
}

fun forEachChunk(player: Player, tile: Tile, block: (Chunk) -> Unit) {
    val area = tile.chunk.toCuboid(radius = player.viewport.tileSize shr 5)
    val max = Tile(area.maxX, area.maxY, area.maxPlane).chunk
    val min = Tile(area.minX, area.minY, area.minPlane).chunk
    for (x in min.x..max.x) {
        for (y in min.y..max.y) {
            block(Chunk(x, y, tile.plane))
        }
    }
}