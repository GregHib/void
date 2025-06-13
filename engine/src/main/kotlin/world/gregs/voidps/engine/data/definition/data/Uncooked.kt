package world.gregs.voidps.engine.data.definition.data

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.client.ui.chat.toIntRange

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
    val rangeOnly: Boolean = false,
) {

    companion object {
        operator fun invoke(reader: ConfigReader): Uncooked {
            var level = 1
            var xp = 0.0
            var burntXp = 0.0
            var chanceMin = 255
            var chanceMax = 255
            var cooked = ""
            var cookedMessage = ""
            var burnt = ""
            var burntMessage = ""
            var leftover = ""
            var start = 1
            var ticks = 4
            var type = "cook"
            val chances = Object2ObjectOpenHashMap<String, IntRange>(4, Hash.VERY_FAST_LOAD_FACTOR)
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    "burntXp" -> burntXp = reader.double()
                    "chance_min" -> chanceMin = reader.int()
                    "chance_max" -> chanceMax = reader.int()
                    "cooked" -> cooked = reader.string()
                    "cooked_message" -> cookedMessage = reader.string()
                    "burnt" -> burnt = reader.string()
                    "burnt_message" -> burntMessage = reader.string()
                    "leftover" -> leftover = reader.string()
                    "start" -> start = reader.int()
                    "ticks" -> ticks = reader.int()
                    "type" -> type = reader.string()
                    "chances" -> while (reader.nextEntry()) {
                        chances[reader.key()] = reader.string().toIntRange()
                    }
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            val fireChance = chances["fire"] ?: EMPTY.chance
            val rangeChance = chances["range"] ?: fireChance
            val cooksRangeChance = chances["cooks_range"] ?: rangeChance
            val gauntletChance = chances["gauntlet"] ?: rangeChance
            return Uncooked(
                level = level,
                xp = xp,
                burntXp = burntXp,
                chance = chanceMin until chanceMax,
                rangeChance = rangeChance,
                cooksRangeChance = cooksRangeChance,
                gauntletChance = gauntletChance,
                cooked = cooked,
                cookedMessage = cookedMessage,
                burnt = burnt,
                burntMessage = burntMessage,
                leftover = leftover,
                start = start,
                ticks = ticks,
                type = type,
                rangeOnly = chances.containsKey("range") && !chances.containsKey("fire"),
            )
        }

        val EMPTY = Uncooked()
    }
}
