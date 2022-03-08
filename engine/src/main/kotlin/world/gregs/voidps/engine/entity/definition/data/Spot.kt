package world.gregs.voidps.engine.entity.definition.data

import world.gregs.voidps.engine.entity.definition.ItemDefinitions
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
        operator fun invoke(map: Map<String, Any>, itemDefinitions: ItemDefinitions): Spot {
            return Spot(
                tackle = (map["items"] as List<String>).map { Item(it, def = itemDefinitions.get(it)) },
                bait = (map["bait"] as Map<String, List<String>>).mapValues { it.value.map { value -> Item(value, def = itemDefinitions.get(value)) } }
            )
        }

        val EMPTY = Spot()
    }
}