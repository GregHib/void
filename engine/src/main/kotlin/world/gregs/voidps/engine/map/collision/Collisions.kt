package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionFlagMap
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

object Collisions {
    val map = CollisionFlagMap()

    operator fun get(absoluteX: Int, absoluteZ: Int, level: Int) = map[absoluteX, absoluteZ, level]
    operator fun set(absoluteX: Int, absoluteZ: Int, level: Int, mask: Int) = map.set(absoluteX, absoluteZ, level, mask)
    fun add(absoluteX: Int, absoluteZ: Int, level: Int, mask: Int) = map.add(absoluteX, absoluteZ, level, mask)
    fun remove(absoluteX: Int, absoluteZ: Int, level: Int, mask: Int) = map.remove(absoluteX, absoluteZ, level, mask)
    fun allocateIfAbsent(absoluteX: Int, absoluteZ: Int, level: Int) = map.allocateIfAbsent(absoluteX, absoluteZ, level)
    fun deallocateIfPresent(absoluteX: Int, absoluteZ: Int, level: Int) = map.deallocateIfPresent(absoluteX, absoluteZ, level)
    fun isZoneAllocated(absoluteX: Int, absoluteZ: Int, level: Int) = map.isZoneAllocated(absoluteX, absoluteZ, level)

    /**
     * Moves [mask] from one tile to another, with a single zone lookup.
     */
    fun move(fromX: Int, fromY: Int, fromLevel: Int, toX: Int, toY: Int, toLevel: Int, mask: Int) {
        val fromZone = zoneIndex(fromX, fromY, fromLevel)
        val toZone = zoneIndex(toX, toY, toLevel)
        val from = map.flags[fromZone]
        if (from != null) {
            val index = tileIndex(fromX, fromY)
            from[index] = from[index] and mask.inv()
        }
        val to = if (toZone == fromZone && from != null) from else map.allocateIfAbsent(toX, toY, toLevel)
        val index = tileIndex(toX, toY)
        to[index] = to[index] or mask
    }

    fun clear() {
        map.flags.fill(null)
    }

    private fun tileIndex(x: Int, y: Int): Int = (x and 0x7) or ((y and 0x7) shl 3)

    private fun zoneIndex(x: Int, y: Int, level: Int): Int = ((x shr 3) and 0x7FF) or (((y shr 3) and 0x7FF) shl 11) or ((level and 0x3) shl 22)
}

fun Collisions.check(x: Int, y: Int, level: Int, flag: Int): Boolean = get(x, y, level) and flag != 0

fun Collisions.check(tile: Tile, flag: Int) = check(tile.x, tile.y, tile.level, flag)

fun Collisions.print(zone: Zone) {
    for (y in 7 downTo 0) {
        for (x in 0 until 8) {
            val value = get(zone.tile.x + x, zone.tile.y + y, zone.level)
            print("${if (value == 0) 0 else 1} ")
        }
        println()
    }
    println()
}

fun Collisions.clear(zone: Zone) {
    deallocateIfPresent(zone.tile.x, zone.tile.y, zone.level)
}

fun Area.random(character: Character): Tile? = random(character.collision, character.size, character.blockMove)

fun Area.random(collision: CollisionStrategy = CollisionStrategies.Normal, size: Int = 1, extraFlag: Int = 0): Tile? {
    val steps = get<StepValidator>()
    var tile = random()
    var exit = 100
    while (!steps.canFit(tile, collision, size, extraFlag)) {
        if (--exit <= 0) {
            return null
        }
        tile = random()
    }
    return tile
}

fun StepValidator.canFit(tile: Tile, collision: CollisionStrategy, size: Int, extraFlag: Int): Boolean {
    if (size != 1) {
        for (i in 1 until size) {
            if (!canTravel(tile.level, tile.x - i, tile.y, 1, 0, size, extraFlag)) {
                return false
            }
            if (!canTravel(tile.level, tile.x, tile.y - i, 0, 1, size, extraFlag)) {
                return false
            }
            if (!canTravel(tile.level, tile.x + i, tile.y, -1, 0, size, extraFlag)) {
                return false
            }
            if (!canTravel(tile.level, tile.x, tile.y + i, 0, -1, size, extraFlag)) {
                return false
            }
        }
        return true
    }
    return canTravel(x = tile.x, z = tile.y - 1, level = tile.level, offsetX = 0, offsetZ = 1, size = size, collision = collision, extraFlag = extraFlag) ||
        canTravel(x = tile.x, z = tile.y + 1, level = tile.level, offsetX = 0, offsetZ = -1, size = size, collision = collision, extraFlag = extraFlag) ||
        canTravel(
            x = tile.x - 1,
            z = tile.y,
            level = tile.level,
            offsetX = 1,
            offsetZ = 0,
            size = size,
            collision = collision,
            extraFlag = extraFlag,
        ) ||
        canTravel(x = tile.x + 1, z = tile.y, level = tile.level, offsetX = -1, offsetZ = 0, size = size, collision = collision, extraFlag = extraFlag)
}
