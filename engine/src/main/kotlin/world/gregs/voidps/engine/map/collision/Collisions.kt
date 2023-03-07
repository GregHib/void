package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.collision.CollisionFlagMap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.RegionPlane

typealias Collisions = CollisionFlagMap

fun Collisions.check(x: Int, y: Int, plane: Int, flag: Int): Boolean {
    return get(x, y, plane) and flag != 0
}

fun Collisions.check(tile: Tile, flag: Int) = check(tile.x, tile.y, tile.plane, flag)

/**
 * Note:
 *  Only suitable for copying of tile collisions, object definitions flags would require transformations
 *  Could accidentally copy collisions of characters active in [from]
 */
fun Collisions.copy(from: Chunk, to: Chunk, rotation: Int) {
//        val array = data[from.regionPlane.id]?.clone() ?: return
//        for (x in 0 until 8) {
//            for (y in 0 until 8) {
//                val toX = if (rotation == 1) y else if (rotation == 2) 7 - x else if (rotation == 3) 7 - y else x
//                val toY = if (rotation == 1) 7 - x else if (rotation == 2) 7 - y else if (rotation == 3) x else y
//                val value = CollisionFlag.rotate(array[index(from.tile.x + x, from.tile.y + y)], rotation)
//                set(to.tile.x + toX, to.tile.y + toY, to.plane, value)
//            }
//        }
}

fun Collisions.clear(region: RegionPlane) {
//        data[region.id]?.fill(0)
}