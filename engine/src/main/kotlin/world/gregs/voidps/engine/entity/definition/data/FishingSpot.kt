package world.gregs.voidps.engine.entity.definition.data

import world.gregs.voidps.engine.entity.item.Item

data class FishingSpot(
    val tackle: List<Item>,
    val bait: Map<String, List<Item>>
) {
    val minimumLevel: Int
        get() = bait.keys.minOf { minimumLevel(it) ?: Int.MAX_VALUE }

    fun minimumLevel(bait: String): Int? {
        return this.bait[bait]?.minOf { it.def["fishing", FishingCatch.EMPTY].level }
    }

    companion object {
        val EMPTY = FishingSpot(emptyList(), emptyMap())

        operator fun invoke(value: Any): FishingSpot {
            val map = value as Map<String, Any>
            return FishingSpot(
                tackle = (map["items"] as List<String>).map { Item(it) },
                bait = (map["bait"] as Map<String, List<String>>).mapValues { it.value.map { Item(it) } }
            )
        }
    }
}