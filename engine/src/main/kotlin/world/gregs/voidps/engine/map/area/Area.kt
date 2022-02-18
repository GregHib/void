package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.strategy.LandCollision
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import world.gregs.voidps.engine.path.traverse.traversal
import world.gregs.voidps.engine.utility.get

interface Area {
    val area: Double

    operator fun contains(tile: Tile): Boolean = contains(tile.x, tile.y, tile.plane)

    fun contains(x: Int, y: Int, plane: Int = 0): Boolean

    fun random(): Tile

    fun random(collisions: Collisions, character: Character): Tile? = random(collisions, character.traversal, character.collision)

    fun random(collisions: Collisions, traversal: TileTraversalStrategy = SmallTraversal, collision: CollisionStrategy = get<LandCollision>()): Tile? {
        var tile = random()
        var exit = 100
        while (traversal.blocked(collision, tile, Size.ONE, Direction.NONE)) {
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