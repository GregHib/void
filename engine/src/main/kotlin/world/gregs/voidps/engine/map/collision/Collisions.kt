package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionFlagMap
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

typealias Collisions = CollisionFlagMap

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

fun Area.random(character: Character): Tile? = random(character.collision, character.size, if (character is NPC && character.def["solid", true]) CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS else 0)

fun Area.random(collision: CollisionStrategy = CollisionStrategies.Normal, size: Int = 1, extraFlag: Int = 0): Tile? {
    val steps = get<StepValidator>()
    var tile = random()
    var exit = 100
    while (!canFit(steps, tile, collision, size, extraFlag)) {
        if (--exit <= 0) {
            return null
        }
        tile = random()
    }
    return tile
}

private fun canFit(steps: StepValidator, tile: Tile, collision: CollisionStrategy, size: Int, extraFlag: Int): Boolean {
    if (size != 1) {
        for (i in 1 until size) {
            if (!steps.canTravel(tile.level, tile.x - i, tile.y, 1, 0, size, extraFlag)) {
                return false
            }
            if (!steps.canTravel(tile.level, tile.x, tile.y - i, 0, 1, size, extraFlag)) {
                return false
            }
            if (!steps.canTravel(tile.level, tile.x + i, tile.y, -1, 0, size, extraFlag)) {
                return false
            }
            if (!steps.canTravel(tile.level, tile.x, tile.y + i, 0, -1, size, extraFlag)) {
                return false
            }
        }
        return true
    }
    return steps.canTravel(x = tile.x, z = tile.y - 1, level = tile.level, offsetX = 0, offsetZ = 1, size = size, collision = collision, extraFlag = extraFlag) || steps.canTravel(x = tile.x, z = tile.y + 1, level = tile.level, offsetX = 0, offsetZ = -1, size = size, collision = collision, extraFlag = extraFlag) || steps.canTravel(x = tile.x - 1, z = tile.y, level = tile.level, offsetX = 1, offsetZ = 0, size = size, collision = collision, extraFlag = extraFlag) || steps.canTravel(x = tile.x + 1, z = tile.y, level = tile.level, offsetX = -1, offsetZ = 0, size = size, collision = collision, extraFlag = extraFlag)
}
