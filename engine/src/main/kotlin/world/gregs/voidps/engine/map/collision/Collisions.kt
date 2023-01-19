package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.collision.CollisionFlagMap
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.RegionPlane

typealias Collisions = CollisionFlagMap

fun Collisions.check(x: Int, y: Int, plane: Int, flag: Int): Boolean {
    return get(x, y, plane) and flag != 0
}

fun Collisions.check(tile: Tile, flag: Int) = check(tile.x, tile.y, tile.plane, flag)

fun Collisions.add(char: Character) {
    for (x in 0 until char.size.width) {
        for (y in 0 until char.size.height) {
            add(char.tile.x + x, char.tile.y + y, char.tile.plane, entity(char))
        }
    }
}

fun Collisions.remove(char: Character) {
    for (x in 0 until char.size.width) {
        for (y in 0 until char.size.height) {
            remove(char.tile.x + x, char.tile.y + y, char.tile.plane, entity(char))
        }
    }
}

fun Collisions.move(character: Character, from: Tile, to: Tile) {
    for (x in 0 until character.size.width) {
        for (y in 0 until character.size.height) {
            remove(from.x + x, from.y + y, from.plane, entity(character))
        }
    }
    for (x in 0 until character.size.width) {
        for (y in 0 until character.size.height) {
            add(to.x + x, to.y + y, to.plane, entity(character))
        }
    }
}

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

private fun entity(character: Character): Int = if (character is Player) CollisionFlag.BLOCK_PLAYERS else (CollisionFlag.BLOCK_NPCS or if (character["solid", false]) CollisionFlag.FLOOR else 0)
