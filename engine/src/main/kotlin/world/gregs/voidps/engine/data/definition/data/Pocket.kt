package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the properties of a pocket configuration, detailing various attributes
 * and their corresponding effects. The pocket can define its level, the experience
 * gained from interactions, stun properties, success chance, and a custom message
 * for caught scenarios.
 *
 * @property level The level associated with the pocket. Default is 1.
 * @property xp The experience points rewarded for relevant interactions. Default is 0.0.
 * @property stunHit The number of hits required to stun. Default is 0.
 * @property stunTicks The duration in ticks during which the pocket is stunned. Default is 1.
 * @property chance The range of chance influencing success or failure rates. Default is 1..1.
 * @property caughtMessage The message displayed upon being caught. Default is "What do you think you're doing?".
 */
data class Pocket(
    val level: Int = 1,
    val xp: Double = 0.0,
    val stunHit: Int = 0,
    val stunTicks: Int = 1,
    val chance: IntRange = 1..1,
    val caughtMessage: String = "What do you think you're doing?"
) {
    /**
     * Companion object for the Pocket class.
     * Provides utility methods and constants for creating and managing Pocket instances.
     */
    companion object {

        /**
         * Creates a Pocket instance based on the provided map of values.
         * If the map doesn't contain expected keys or the values are of an invalid type, default values from `EMPTY` are used.
         *
         * @param map A map containing keys and corresponding values for the Pocket properties.
         * - `level`: Int representing the level, or defaults to `EMPTY.level`.
         * - `xp`: Double representing the experience points, or defaults to `EMPTY.xp`.
         * - `stun_hit`: Int representing stun hits, or defaults to `EMPTY.stunHit`.
         * - `stun_ticks`: Int representing stun duration in ticks, or defaults to `EMPTY.stunTicks`.
         * - `chance`: IntRange representing a range of chances, or defaults to `EMPTY.chance`.
         * - `caught`: String representing the caught message, or defaults to `EMPTY.caughtMessage`.
         */
        operator fun invoke(map: Map<String, Any>) = Pocket(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            stunHit = map["stun_hit"] as? Int ?: EMPTY.stunHit,
            stunTicks = map["stun_ticks"] as? Int ?: EMPTY.stunTicks,
            chance = map["chance"] as? IntRange ?: EMPTY.chance,
            caughtMessage = map["caught"] as? String ?: EMPTY.caughtMessage,
        )

        /**
         * A predefined constant representing an instance of the Pocket class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Pocket()
    }
}