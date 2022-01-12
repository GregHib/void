package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.map.Tile

data class Size(val width: Int, val height: Int) {
    companion object {
        val ONE = Size(1, 1)
        val TWO = Size(2, 2)
    }
}

fun Tile.add(size: Size) = add(size.width, size.height)