package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy

interface Area {

    val area: Double
    val region: Region
    val regions: Set<Region>

    operator fun contains(tile: Tile): Boolean

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
}