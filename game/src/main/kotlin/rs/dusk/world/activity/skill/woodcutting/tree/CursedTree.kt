package rs.dusk.world.activity.skill.woodcutting.tree

import rs.dusk.world.activity.skill.woodcutting.log.CursedLog

@Suppress("EnumEntryName")
enum class CursedTree(
    override val log: CursedLog,
    override val level: Int,
    override val xp: Double,
    override val fellRate: Double
) : Tree {
    Cursed_Willow_Roots(CursedLog.Cursed_Willow_Logs, 37, 15.0, 0.125),
    Cursed_Magic_Tree(CursedLog.Cursed_Magic_Logs, 82, 275.0, 0.125);

    override val id: String = name.toLowerCase()
}