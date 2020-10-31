package rs.dusk.world.activity.skill.woodcutting.tree

import rs.dusk.world.activity.skill.woodcutting.log.CursedLog

@Suppress("EnumEntryName")
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
    Cursed_Willow_Roots(CursedLog.Cursed_Willow_Logs, 37, 15.0, 0.125, 0..0, 0..0, 0..0, 0..0),
    Cursed_Magic_Tree(CursedLog.Cursed_Magic_Logs, 82, 275.0, 0.125, 0..0, 0..0, 0..0, 150..300);

    override val id: String = name.toLowerCase()
}