package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.collision.CollisionFlagMap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk

typealias Collisions = CollisionFlagMap

fun Collisions.check(x: Int, y: Int, plane: Int, flag: Int): Boolean {
    return get(x, y, plane) and flag != 0
}

fun Collisions.check(tile: Tile, flag: Int) = check(tile.x, tile.y, tile.plane, flag)

fun Collisions.clear(chunk: Chunk) {
    deallocate(chunk.tile.x, chunk.tile.y, chunk.plane)
    allocateIfAbsent(chunk.tile.x, chunk.tile.y, chunk.plane)
}