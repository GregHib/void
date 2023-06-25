package world.gregs.voidps.engine.data.definition.data

import world.gregs.voidps.engine.entity.item.Item

data class Spot(
    val tackle: List<Item> = emptyList(),
    val bait: Map<String, List<Item>> = emptyMap()
) {
    val minimumLevel: Int
        get() = bait.keys.minOf { minimumLevel(it) ?: Int.MAX_VALUE }

    fun minimumLevel(bait: String): Int? {
        return this.bait[bait]?.minOf { it.def["fishing", Catch.EMPTY].level }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>): Spot {
            return Spot(
                tackle = map["items"] as List<Item>,
                bait = map["bait"] as Map<String, List<Item>>
            )
        }

        val EMPTY = Spot()
    }
}