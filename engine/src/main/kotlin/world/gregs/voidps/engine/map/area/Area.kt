package world.gregs.voidps.engine.map.area

import org.rsmod.pathfinder.StepValidator
import org.rsmod.pathfinder.collision.CollisionStrategies
import org.rsmod.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.utility.get

interface Area {
    val area: Double

    operator fun contains(tile: Tile): Boolean = contains(tile.x, tile.y, tile.plane)

    fun contains(x: Int, y: Int, plane: Int = 0): Boolean

    fun random(): Tile

    fun random(collisions: Collisions, character: Character): Tile? = random(collisions, character.collision)

    fun random(collisions: Collisions, collision: CollisionStrategy = CollisionStrategies.Normal): Tile? {
        val steps = get<StepValidator>()
        var tile = random()
        var exit = 100
        while (!steps.canTravel(tile.x, tile.y, tile.plane, 1, 0, 0, 0, collision)) {
            if (--exit <= 0) {
                return null
            }
            tile = random()
        }
        return tile
    }

    fun toRegions(): List<Region>

    fun toChunks(): List<Chunk>
}