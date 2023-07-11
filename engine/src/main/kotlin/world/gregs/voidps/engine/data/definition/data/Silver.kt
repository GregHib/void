package world.gregs.voidps.engine.data.definition.data

/**
 * @param name interface override
 * @param item the silver item to craft
 * @param level required to attempt to craft
 * @param xp experience from successfully crafting
 * @param quest quest required to display this item
 */
data class Silver(
    val name: String? = null,
    val item: String = "",
    val xp: Double = 0.0,
    val level: Int = 1,
    val quest: String? = null
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Silver(
            name = map["name"] as? String ?: EMPTY.name,
            item = map["item"] as? String ?: EMPTY.item,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            level = map["level"] as? Int ?: EMPTY.level,
            quest = map["quest"] as? String ?: EMPTY.quest,
        )

        val EMPTY = Silver()
    }
}