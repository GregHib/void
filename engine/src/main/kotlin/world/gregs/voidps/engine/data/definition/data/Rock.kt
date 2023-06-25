package world.gregs.voidps.engine.data.definition.data

import world.gregs.voidps.engine.entity.item.Item

/**
 * @param level required to attempt to mine
 * @param ores List of materials that can be mined
 * @param life duration in ticks that the fire object will last for
 * @param gems if rock has chance of dropping random gems
 */
data class Rock(
    val level: Int = 1,
    val ores: List<Item> = emptyList(),
    val life: Int = -1,
    val gems: Boolean = false
) {
    companion object {

        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = Rock(
            level = map["level"] as? Int ?: EMPTY.level,
            ores = map["ores"] as? List<Item> ?: EMPTY.ores,
            life = map["life"] as? Int ?: EMPTY.life,
            gems = map["gems"] as? Boolean ?: EMPTY.gems,
        )

        val EMPTY = Rock()
    }
}