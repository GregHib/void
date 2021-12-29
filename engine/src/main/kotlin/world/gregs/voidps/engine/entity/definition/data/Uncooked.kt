package world.gregs.voidps.engine.entity.definition.data

import world.gregs.voidps.engine.utility.toIntRange

/**
 * @param level required to attempt cooking
 * @param xp experience for successfully cooking
 * @param chance of burning on a fire
 * @param rangeChance of burning on a stove
 * @param cooksRangeChance of burning on the lumbridge range
 * @param gauntletChance of burning with cooking_gauntlets
 * @param cooked cooked item name if not `"raw_item".replace("raw", "cooked")`
 * @param burnt burnt item name if not `"raw_item".replace("raw", "burnt")`
 * @param leftover any by-products from cooking
 * @param start tick delay
 * @param ticks till the end of the process
 */
data class Uncooked(
    val level: Int = 1,
    val xp: Double = 0.0,
    val burntXp: Double = 0.0,
    val chance: IntRange = 255..255,
    val rangeChance: IntRange = chance,
    val cooksRangeChance: IntRange = rangeChance,
    val gauntletChance: IntRange = rangeChance,
    val cooked: String = "",
    val cookedMessage: String = "",
    val burnt: String = "",
    val burntMessage: String = "",
    val leftover: String = "",
    val start: Int = 1,
    val ticks: Int = 4,
    val type: String = "cook",
    val rangeOnly: Boolean = false
) {

    companion object {

        operator fun invoke(map: Map<String, Any>): Uncooked {
            val chances = map["chances"] as? Map<String, String> ?: emptyMap()
            val fireChance = chances["fire"]?.toIntRange() ?: EMPTY.chance
            val rangeChance = chances["range"]?.toIntRange() ?: fireChance
            val cooksRangeChance = chances["cooks_range"]?.toIntRange() ?: rangeChance
            val gauntletChance = chances["gauntlet"]?.toIntRange() ?: rangeChance
            return Uncooked(
                level = map["level"] as? Int ?: EMPTY.level,
                xp = map["xp"] as? Double ?: EMPTY.xp,
                chance = fireChance,
                rangeChance = rangeChance,
                cooksRangeChance = cooksRangeChance,
                gauntletChance = gauntletChance,
                cooked = map["cooked"] as? String ?: EMPTY.cooked,
                cookedMessage = map["cooked_message"] as? String ?: EMPTY.cookedMessage,
                burnt = map["burnt"] as? String ?: EMPTY.burnt,
                burntMessage = map["burnt_message"] as? String ?: EMPTY.burntMessage,
                leftover = map["leftover"] as? String ?: EMPTY.leftover,
                type = map["type"] as? String ?: EMPTY.type,
                rangeOnly = chances.containsKey("range") && !chances.containsKey("fire")
            )
        }

        val EMPTY = Uncooked()
    }
}