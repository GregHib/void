package world.gregs.voidps.engine.data.definition.data

data class Tanning(
    val prices: List<List<Any>> = emptyList()
) {
    companion object {
        val EMPTY = Tanning()
    }
}