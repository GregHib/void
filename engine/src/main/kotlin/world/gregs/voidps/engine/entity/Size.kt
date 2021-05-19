package world.gregs.voidps.engine.entity

data class Size(val width: Int, val height: Int) {
    companion object {
        val TILE = Size(1, 1)
    }
}