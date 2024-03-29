package world.gregs.voidps.engine.data.definition.data

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
        val EMPTY = Tree()
    }
}