package world.gregs.voidps.engine.data.definition.data

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * Represents a Rune entity containing data relevant to runecrafting.
 *
 * @property xp The experience gained from crafting this rune. Default is 0.0.
 * @property pure A flag indicating whether this is a pure rune or not. Default is false.
 * @property levels The required runecrafting levels for crafting this rune, represented as an array of integers.
 * @property combinations A map of combination runes and their respective components.
 * @property doubleChance The chance of crafting double runes. Default is 0.0.
 */
data class Rune(
    val xp: Double = 0.0,
    val pure: Boolean = false,
    val levels: IntArray = intArrayOf(),
    val combinations: Map<String, List<Any>> = emptyMap(),
    val doubleChance: Double = 0.0
) {
    /**
     * Determines the multiplier based on the player's Runecrafting skill level.
     *
     * @param player The player whose levels are being evaluated.
     * @return The calculated multiplier based on the player's Runecrafting level.
     */
    fun multiplier(player: Player): Int {
        var multiplier = 1
        for (index in levels.indices.reversed()) {
            if (player.levels.get(Skill.Runecrafting) >= levels[index]) {
                multiplier = index + 1
                break
            }
        }
        return multiplier
    }

    /**
     * Checks whether the specified object is equal to this instance.
     *
     * @param other the object to be compared for equality with this instance.
     * @return true if the specified object is equal to this instance; false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rune

        if (xp != other.xp) return false
        if (pure != other.pure) return false
        return levels.contentEquals(other.levels)
    }

    /**
     * Computes the hash code for this object based on its properties.
     *
     * @return The hash code value as an integer, computed using the properties `xp`, `pure`, and `levels`.
     */
    override fun hashCode(): Int {
        var result = xp.hashCode()
        result = 31 * result + pure.hashCode()
        result = 31 * result + levels.contentHashCode()
        return result
    }

    /**
     * Companion object for the Rune class. Provides utilities such as the `invoke` operator
     * for creating a Rune instance from a map and a default EMPTY instance.
     */
    companion object {

        /**
         * Converts a map of values into a `Rune` object by extracting and casting
         * the required properties. If a particular key is not found in the map or
         * if the value is of an incorrect type, a default value from `EMPTY` is used instead.
         *
         * @param map A map where the keys represent property names and the values
         *            are the corresponding data used to create a `Rune`.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = Rune(
            xp = map["xp"] as? Double ?: EMPTY.xp,
            pure = map["pure"] as? Boolean ?: EMPTY.pure,
            levels = (map["levels"] as? List<Int>)?.toIntArray() ?: EMPTY.levels,
            combinations = (map["combinations"] as? Map<String, List<Any>>) ?: EMPTY.combinations,
            doubleChance = (map["ourania_chance"] as? Double) ?: EMPTY.doubleChance,
        )

        /**
         * A constant representing an empty or default rune. This value can be used
         * as a placeholder or to signify the absence of a meaningful rune.
         */
        val EMPTY = Rune()
    }
}