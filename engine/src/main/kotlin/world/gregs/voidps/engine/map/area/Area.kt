package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region

interface Area {
    operator fun contains(tile: Tile): Boolean

    fun random(): Tile

    val area: Double
    val region: Region
}