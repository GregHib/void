package world.gregs.voidps.world.activity.skill.woodcutting.tree

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.woodcutting.log.CursedLog

enum class CursedTree(
    override val log: CursedLog,
    override val level: Int,
    override val xp: Double,
    override val depleteRate: Double,
    override val chance: IntRange,
    override val lowDifference: IntRange,
    override val highDifference: IntRange,
    override val respawnDelay: IntRange
) : Tree {
    CursedWillowRoots(CursedLog.CursedWillowLogs, 37, 15.0, 0.125, 0..0, 0..0, 0..0, 0..0),
    CursedMagicTree(CursedLog.CursedMagicLogs, 82, 275.0, 0.125, 0..0, 0..0, 0..0, 150..300);

    override val id: String = name.toTitleCase().toUnderscoreCase()
}