package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy

interface Area {
    val area: Double

    operator fun contains(tile: Tile): Boolean = contains(tile.x, tile.y, tile.plane)

    fun contains(x: Int, y: Int, plane: Int = 0): Boolean

    fun random(): Tile

    fun random(traversal: TileTraversalStrategy): Tile? {
        var tile = random()
        var exit = 100
        while (traversal.blocked(tile, Direction.NONE)) {
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