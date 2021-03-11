package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.map.Tile

interface Area {
    operator fun contains(tile: Tile): Boolean

    fun random(): Tile

    val area: Double
}