package world.gregs.voidps.engine.data.definition.data

/**
 * Represents a tree with properties to define its behavior in a game or simulation.
 *
 * @param log The type of log associated with the tree.
 * @param level The required level to interact with the tree.
 * @param xp The experience rewarded for processes involving the tree.
 * @param depleteRate The rate at which the tree depletes during interactions.
 * @param chance The range of chance influencing the success of interactions with the tree.
 * @param hatchetLowDifference The range of effectiveness difference for low-quality hatchets.
 * @param hatchetHighDifference The range of effectiveness difference for high-quality hatchets.
 * @param respawnDelay The range of delay in ticks before the tree respawns after being depleted.
 */
data class Tree(
    val log: String = "",
    val level: Int = 1,
    val xp: Double = 0.0,
    val depleteRate: Double = 1.0,
    val chance: IntRange = 0..0,
    val hatchetLowDifference: IntRange = 0..0,
    val hatchetHighDifference: IntRange = 0..0,
    val respawnDelay: IntRange = 0..0
) {
    /**
     * Companion object for the Tree class.
     * Provides utility methods and constants for creating and managing Tree instances.
     */
    companion object {
        /**
         * Creates a Tree instance using values from the provided map.
         * Defaults to `EMPTY` values for missing or invalid entries in the map.
         *
         * @param map A map containing key-value pairs for initializing the Tree instance:
         * - `log`: String representing the log type, default is `EMPTY.log`.
         * - `level`: Int representing the level requirement, default is `EMPTY.level`.
         * - `xp`: Double representing the experience points, default is `EMPTY.xp`.
         * - `deplete_rate`: Double representing the rate of resource depletion, default is `EMPTY.depleteRate`.
         * - `chance`: IntRange representing the range of chance, default is `EMPTY.chance`.
         * - `hatchet_low_dif`: IntRange representing the lower difference for hatchet levels, default is `EMPTY.hatchetLowDifference`.
         * - `hatchet_high_dif`: IntRange representing the higher difference for hatchet levels, default is `EMPTY.hatchetHighDifference`.
         * - `respawn`: IntRange representing the respawn delay interval, default is `EMPTY.respawnDelay`.
         */
        operator fun invoke(map: Map<String, Any>) = Tree(
            log = map["log"] as? String ?: EMPTY.log,
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            depleteRate = map["deplete_rate"] as? Double ?: EMPTY.depleteRate,
            chance = map["chance"] as? IntRange ?: EMPTY.chance,
            hatchetLowDifference = map["hatchet_low_dif"] as? IntRange ?: EMPTY.hatchetLowDifference,
            hatchetHighDifference = map["hatchet_high_dif"] as? IntRange ?: EMPTY.hatchetHighDifference,
            respawnDelay = map["respawn"] as? IntRange ?: EMPTY.respawnDelay
        )
        /**
         * A predefined constant representing a Tree instance with default values for all properties.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Tree()
    }
}