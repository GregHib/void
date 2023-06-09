package world.gregs.voidps.engine.entity

data class Size(val width: Int, val height: Int) {
    companion object {
        val ONE = Size(1, 1)
        val TWO = Size(2, 2)
    }
}