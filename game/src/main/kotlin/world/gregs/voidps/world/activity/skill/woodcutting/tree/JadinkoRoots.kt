package world.gregs.voidps.world.activity.skill.woodcutting.tree

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.woodcutting.log.JadinkoRoot

enum class JadinkoRoots(
    override val log: JadinkoRoot,
    override val level: Int,
    override val xp: Double,
    override val depleteRate: Double,
    override val chance: IntRange,
    override val lowDifference: IntRange,
    override val highDifference: IntRange,
    override val respawnDelay: IntRange
) : Tree {
    StraightJadeRoots(JadinkoRoot.StraightRoot, 83, 80.5, 1.00, 0..0, 0..0, 0..0, 0..0),
    CurlyJadeRoots(JadinkoRoot.CurlyRoot, 83, 80.8, 1.00, 0..0, 0..0, 0..0, 0..0);

    override val id: String = name.toTitleCase().toUnderscoreCase()
}