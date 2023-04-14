package world.gregs.voidps.engine.map.area

import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Region

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
        while (!steps.canTravel(x = tile.x, z = tile.y, level = tile.plane, size = 1, offsetX = 1, offsetZ = 0, extraFlag = 0, collision = collision)) {
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