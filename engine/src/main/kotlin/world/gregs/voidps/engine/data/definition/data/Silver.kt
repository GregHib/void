package world.gregs.voidps.engine.data.definition.data

/**
 * Represents a Silver entity with properties such as name, item, experience points (XP), level, and quest.
 *
 * @param name The name associated with the Silver entity, default is null.
 * @param item The item represented by the Silver entity, default is an empty string.
 * @param xp The experience points of the Silver entity, default is 0.0.
 * @param level The level value associated with the Silver entity, default is 1.
 * @param quest The quest linked to the Silver entity, if applicable, default is null.
 */
data class Silver(
    val name: String? = null,
    val item: String = "",
    val xp: Double = 0.0,
    val level: Int = 1,
    val quest: String? = null
) {
    /**
     * Companion object for the Silver class.
     * Provides utility methods and constants for creating and managing Silver instances.
     */
    companion object {

        /**
         * Creates an instance of the Silver class using values from the provided map.
         * Default values from `EMPTY` are used for missing or invalid map entries.
         *
         * @param map A map containing keys and values for initializing the Silver instance:
         * - `name`: String representing the name, default is `EMPTY.name`.
         * - `item`: String representing the item, default is `EMPTY.item`.
         * - `xp`: Double representing the experience points, default is `EMPTY.xp`.
         * - `level`: Int representing the level, default is `EMPTY.level`.
         * - `quest`: String representing the quest, default is `EMPTY.quest`.
         */
        operator fun invoke(map: Map<String, Any>) = Silver(
            name = map["name"] as? String ?: EMPTY.name,
            item = map["item"] as? String ?: EMPTY.item,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            level = map["level"] as? Int ?: EMPTY.level,
            quest = map["quest"] as? String ?: EMPTY.quest,
        )

        /**
         * A predefined constant representing an instance of the Silver class initialized with default values.
         * Serves as a placeholder or default value to prevent null references or for initialization purposes.
         */
        val EMPTY = Silver()
    }
}