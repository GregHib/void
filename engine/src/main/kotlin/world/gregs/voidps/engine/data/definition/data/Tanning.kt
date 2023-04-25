package world.gregs.voidps.engine.data.definition.data

data class Tanning(
    val prices: Array<Pair<String, Int>> = emptyArray()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tanning

        if (!prices.contentEquals(other.prices)) return false

        return true
    }

    override fun hashCode(): Int {
        return prices.contentHashCode()
    }

    companion object {

        operator fun invoke(map: Map<String, Any>) = Tanning(map.map { it.key to it.value as Int }.toTypedArray())

        val EMPTY = Tanning()
    }
}