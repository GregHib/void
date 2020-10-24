package rs.dusk.world.activity.skill.woodcutting.tree

import rs.dusk.world.activity.skill.woodcutting.log.Log
import rs.dusk.world.activity.skill.woodcutting.log.MiscLog
import rs.dusk.world.activity.skill.woodcutting.log.RegularLog

@Suppress("EnumEntryName")
enum class RegularTree(
    override val log: Log,
    override val level: Int,
    override val xp: Double,
    override val fellRate: Double
) : Tree {
    Tree(RegularLog.Logs, 1, 25.0, 1.0),
    Dying_Tree(RegularLog.Logs, 1, 25.0, 0.125),
    Dead_Tree(RegularLog.Logs, 1, 25.0, 0.125),
    Evergreen(RegularLog.Logs, 1, 25.0, 0.125),
    Burnt_Tree(RegularLog.Logs, 1, 25.0, 0.125),
    Jungle_Tree(RegularLog.Logs, 1, 25.0, 0.125),
    Swamp_Tree(RegularLog.Logs, 1, 25.0, 0.125),
    Achey_Tree(RegularLog.Achey_Tree_Logs, 1, 25.0, 0.125),
    Oak_Tree(RegularLog.Oak_Logs, 15, 37.5, 0.125),
    Willow_Tree(RegularLog.Willow_Logs, 30, 67.5, 0.125),
    Teak_Tree(RegularLog.Teak_Logs, 35, 85.0, 0.125),
    Maple_Tree(RegularLog.Maple_Logs, 45, 100.0, 0.125),
    Acadia_Tree(RegularLog.Acadia_Logs, 47, 92.0, 0.125),
    Arctic_Pine(RegularLog.Arctic_Pine_Logs, 56, 140.0, 0.125),
    Hollow_Tree(MiscLog.Bark, 45, 357.7, 0.125),
    Eucalyptus_Tree(RegularLog.Eucalyptus_Logs, 58, 165.0, 0.125),
    Mahogany_Tree(RegularLog.Mahogany_Logs, 50, 125.0, 0.125),
    Yew_Tree(RegularLog.Yew_Logs, 60, 175.0, 0.125),
    Magic_Tree(RegularLog.Magic_Logs, 75, 250.0, 0.125),
    Blisterwood_Tree(MiscLog.Blisterwood_Logs, 76, 200.0, 0.25);

    override val id: String = name.toLowerCase()
}