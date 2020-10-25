package rs.dusk.world.activity.skill.woodcutting.tree

import rs.dusk.world.activity.skill.woodcutting.log.Log
import rs.dusk.world.activity.skill.woodcutting.log.MiscLog
import rs.dusk.world.activity.skill.woodcutting.log.RegularLog

/**
 * Note: all data accurate to wiki/skilling chances spreadsheet
 * @param log The log given on success
 * @param level The woodcutting level required to cut
 * @param xp The woodcutting experience given on success
 * @param fellRate The chance on success of a tree falling
 * @param chance The chance out of 256 of success at level 1 and 99
 * @param lowDifference The min and max difference increase in chance per hatchet at level 1
 * @param highDifference The min and max difference increase in chance per hatchet at level 99
 */
@Suppress("EnumEntryName")
enum class RegularTree(
    override val log: Log?,
    override val level: Int,
    override val xp: Double,
    override val fellRate: Double,
    override val chance: IntRange,
    override val lowDifference: IntRange,
    override val highDifference: IntRange
) : Tree {
    Tree(RegularLog.Logs, 1, 25.0, 1.0, 64..200, 16..32, 50..100),
    Dying_Tree(RegularLog.Logs, 1, 25.0, 0.125, 64..200, 16..32, 50..100),
    Dead_Tree(RegularLog.Logs, 1, 25.0, 0.125, 64..200, 16..32, 50..100),
    Evergreen(RegularLog.Logs, 1, 25.0, 0.125, 64..200, 16..32, 50..100),
    Burnt_Tree(RegularLog.Logs, 1, 25.0, 0.125, 64..200, 16..32, 50..100),
    Jungle_Tree(RegularLog.Logs, 1, 25.0, 0.125, 64..200, 16..32, 50..100),
    Swamp_Tree(RegularLog.Logs, 1, 25.0, 0.125, 64..200, 16..32, 50..100),
    Achey_Tree(RegularLog.Achey_Tree_Logs, 1, 25.0, 0.125, 64..200, 16..32, 50..100),
    Oak_Tree(RegularLog.Oak_Logs, 15, 37.5, 0.125, 32..100, 8..16, 25..50),
    Willow_Tree(RegularLog.Willow_Logs, 30, 67.5, 0.125, 16..50, 4..8, 13..25),
    Teak_Tree(RegularLog.Teak_Logs, 35, 85.0, 0.125, 15..46, 4..8, 15..24),
    Maple_Tree(RegularLog.Maple_Logs, 45, 100.0, 0.125, 8..25, 2..4, 6..12),
    Acadia_Tree(RegularLog.Acadia_Logs, 47, 92.0, 0.125, 15..46, 4..8, 15..24),
    Arctic_Pine(RegularLog.Arctic_Pine_Logs, 56, 140.0, 0.125, 6..30, 1..2, 7..14),
    Hollow_Tree(MiscLog.Bark, 45, 357.7, 0.125, 18..26, 4..10, 11..14),
    Eucalyptus_Tree(RegularLog.Eucalyptus_Logs, 58, 165.0, 0.125, 5..16, 1..3, 4..9),
    Mahogany_Tree(RegularLog.Mahogany_Logs, 50, 125.0, 0.125, 8..25, 2..4, 9..13),
    Yew_Tree(RegularLog.Yew_Logs, 60, 175.0, 0.125, 4..12, 1..2, 3..7),
    Magic_Tree(RegularLog.Magic_Logs, 75, 250.0, 0.125, 2..6, 0..1, 2..3),
    Blisterwood_Tree(MiscLog.Blisterwood_Logs, 76, 200.0, 0.25, 4..12, 1..2, 3..7),
    Ivy(null, 68, 332.5, 0.125, 7..11, 2..2, 2..6);

    override val id: String = name.toLowerCase()
}