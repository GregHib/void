package world.gregs.voidps.world.activity.skill.woodcutting.tree

import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.woodcutting.log.Log
import world.gregs.voidps.world.activity.skill.woodcutting.log.MiscLog
import world.gregs.voidps.world.activity.skill.woodcutting.log.RegularLog

/**
 * Note: all data accurate to wiki/skilling chances spreadsheet
 * @param log The log given on success
 * @param level The woodcutting level required to cut
 * @param xp The woodcutting experience given on success
 * @param depleteRate The chance on success of a tree falling
 * @param chance The chance out of 256 of success at level 1 and 99
 * @param lowDifference The min and max difference increase in chance per hatchet at level 1
 * @param highDifference The min and max difference increase in chance per hatchet at level 99
 * @param respawnDelay The delay in ticks before regrowing at 2000 and 0 players online (Taken from https://www.runehq.com/skill/woodcutting#respawntimes and unknown ones balanced around those values)
 */
enum class RegularTree(
    override val log: Log?,
    override val level: Int,
    override val xp: Double,
    override val depleteRate: Double,
    override val chance: IntRange,
    override val lowDifference: IntRange,
    override val highDifference: IntRange,
    override val respawnDelay: IntRange
) : Tree {
    Tree(RegularLog.Logs, 1, 25.0, 1.0, 64..200, 16..32, 50..100, 30..60),
    DyingTree(RegularLog.Logs, 1, 25.0, 1.0, 64..200, 16..32, 50..100, 30..60),
    DeadTree(RegularLog.Logs, 1, 25.0, 1.0, 64..200, 16..32, 50..100, 30..60),
    Evergreen(RegularLog.Logs, 1, 25.0, 1.0, 64..200, 16..32, 50..100, 30..60),
    BurntTree(RegularLog.Logs, 1, 25.0, 1.0, 64..200, 16..32, 50..100, 30..60),
    JungleTree(RegularLog.Logs, 1, 25.0, 1.0, 64..200, 16..32, 50..100, 30..60),
    SwampTree(RegularLog.Logs, 1, 25.0, 1.0, 64..200, 16..32, 50..100, 30..60),
    AcheyTree(RegularLog.AcheyTreeLogs, 1, 25.0, 1.0, 64..200, 16..32, 50..100, 30..60),
    Oak(RegularLog.OakLogs, 15, 37.5, 0.125, 32..100, 8..16, 25..50, 23..45),
    Willow(RegularLog.WillowLogs, 30, 67.5, 0.125, 16..50, 4..8, 13..25, 23..45),
    Teak(RegularLog.TeakLogs, 35, 85.0, 0.125, 15..46, 4..8, 15..24, 40..80),
    MapleTree(RegularLog.MapleLogs, 45, 100.0, 0.125, 8..25, 2..4, 6..12, 58..125),
    ArcticPine(RegularLog.ArcticPineLogs, 56, 140.0, 0.125, 6..30, 1..2, 7..14, 80..160),
    HollowTree(MiscLog.Bark, 45, 82.5, 0.125, 18..26, 4..10, 11..14, 75..150),
    EucalyptusTree(RegularLog.EucalyptusLogs, 58, 165.0, 0.125, 5..16, 1..3, 4..9, 90..180),
    Mahogany(RegularLog.MahoganyLogs, 50, 125.0, 0.125, 8..25, 2..4, 9..13, 70..140),
    Yew(RegularLog.YewLogs, 60, 175.0, 0.125, 4..12, 1..2, 3..7, 100..200),
    MagicTree(RegularLog.MagicLogs, 75, 250.0, 0.125, 2..6, 0..1, 2..3, 200..387),
    BlisterwoodTree(MiscLog.BlisterwoodLogs, 76, 200.0, 0.25, 4..12, 1..2, 3..7, 150..300),
    Ivy(null, 68, 332.5, 0.125, 7..11, 2..2, 2..6, 60..60);

    override val id: String = name.toTitleCase().toUnderscoreCase()

    companion object {
        fun get(gameObject: GameObject): RegularTree? {
            val name = DefinitionsDecoder.toIdentifier(gameObject.def.name)
            return values().firstOrNull { tree -> tree.id == name }
        }
    }
}