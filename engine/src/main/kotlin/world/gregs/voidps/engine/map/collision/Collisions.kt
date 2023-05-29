package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.collision.CollisionFlagMap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk

typealias Collisions = CollisionFlagMap

fun Collisions.check(x: Int, y: Int, plane: Int, flag: Int): Boolean {
    return get(x, y, plane) and flag != 0
}

fun Collisions.check(tile: Tile, flag: Int) = check(tile.x, tile.y, tile.plane, flag)

fun Collisions.print(chunk: Chunk) {
    for (y in 7 downTo 0) {
        for (x in 0 until 8) {
            val value = get(chunk.tile.x + x, chunk.tile.y + y, chunk.plane)
            print("${if (value == 0) 0 else 1} ")
        }
        println()
    }
    println()
}

fun Collisions.clear(chunk: Chunk) {
    deallocateIfPresent(chunk.tile.x, chunk.tile.y, chunk.plane)
}