package world.gregs.voidps.world.activity.skill.firemaking.fire

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.woodcutting.log.RegularLog

enum class RegularFire(
    override val log: RegularLog,
    override val level: Int,
    override val xp: Double,
    override val life: Int
) : Fire {
    Normal(RegularLog.Logs, 1, 40.0, 60),
    Achey(RegularLog.AcheyTreeLogs, 1, 40.0, 60),
    Oak(RegularLog.OakLogs, 15, 60.0, 90),
    Willow(RegularLog.WillowLogs, 30, 90.0, 90),
    Teak(RegularLog.TeakLogs, 35, 105.0, 90),
    ArcticPine(RegularLog.ArcticPineLogs, 42, 125.0, 100),
    Maple(RegularLog.MapleLogs, 45, 135.0, 100),
    Mahogany(RegularLog.MahoganyLogs, 50, 157.5, 140),
    Eucalyptus(RegularLog.EucalyptusLogs, 58, 193.5, 140),
    Yew(RegularLog.YewLogs, 60, 202.5, 160),
    Magic(RegularLog.MagicLogs, 75, 303.8, 180);

    override val id: String = name.toTitleCase().toUnderscoreCase()

    override val chance: IntRange = 65..513
}