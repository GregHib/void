package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.client.ui.chat.toIntRange

/**
 * Note: all regular tree data is accurate to wiki/skilling chances spreadsheet
 * @param log The log given on success
 * @param level The woodcutting level required to cut
 * @param xp The woodcutting experience given on success
 * @param depleteRate The chance on success of a tree falling
 * @param chance The chance out of 256 of success at level 1 and 99
 * @param hatchetLowDifference The min and max difference increase in chance per hatchet at level 1
 * @param hatchetHighDifference The min and max difference increase in chance per hatchet at level 99
 * @param respawnDelay The delay in ticks before regrowing at 2000 and 0 players online (Taken from https://www.runehq.com/skill/woodcutting#respawntimes and unknown ones balanced around those values)
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
    companion object {
        operator fun invoke(reader: ConfigReader): Tree {
            var log = ""
            var level = 1
            var xp = 0.0
            var depleteRate = 1.0
            var chance: IntRange = 0..0
            var hatchetLowDifference: IntRange = 0..0
            var hatchetHighDifference: IntRange = 0..0
            var respawnDelay: IntRange = 0..0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "log" -> log = reader.string()
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    "deplete_rate" -> depleteRate = reader.double()
                    "chance" -> chance = reader.string().toIntRange()
                    "hatchet_low_dif" -> hatchetLowDifference = reader.string().toIntRange()
                    "hatchet_high_dif" -> hatchetHighDifference = reader.string().toIntRange()
                    "respawn" -> respawnDelay = reader.string().toIntRange()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Tree(log = log, level = level, xp = xp, depleteRate = depleteRate, chance = chance, hatchetLowDifference = hatchetLowDifference, hatchetHighDifference = hatchetHighDifference, respawnDelay = respawnDelay)
        }

        val EMPTY = Tree()
    }
}