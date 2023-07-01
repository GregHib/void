package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionFlagMap
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
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


fun Area.random(collisions: Collisions, character: Character): Tile? = random(collisions, character.collision)

fun Area.random(collisions: Collisions, collision: CollisionStrategy = CollisionStrategies.Normal): Tile? {
    val steps = get<StepValidator>()
    var tile = random()
    var exit = 100
    while (!canTravel(steps, tile, collision)) {
        if (--exit <= 0) {
            return null
        }
        tile = random()
    }
    return tile
}

private fun canTravel(steps: StepValidator, tile: Tile, collision: CollisionStrategy) =
    steps.canTravel(x = tile.x, z = tile.y - 1, level = tile.plane, size = 1, offsetX = 0, offsetZ = 1, extraFlag = 0, collision = collision) ||
            steps.canTravel(x = tile.x, z = tile.y + 1, level = tile.plane, size = 1, offsetX = 0, offsetZ = -1, extraFlag = 0, collision = collision) ||
            steps.canTravel(x = tile.x - 1, z = tile.y, level = tile.plane, size = 1, offsetX = 1, offsetZ = 0, extraFlag = 0, collision = collision) ||
            steps.canTravel(x = tile.x + 1, z = tile.y, level = tile.plane, size = 1, offsetX = -1, offsetZ = 0, extraFlag = 0, collision = collision)

