package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase

enum class RegularLog : Log {
    Logs,
    AcheyTreeLogs,
    OakLogs,
    WillowLogs,
    TeakLogs,
    MapleLogs,
    AcadiaLogs,
    MahoganyLogs,
    ArcticPineLogs,
    EucalyptusLogs,
    YewLogs,
    MagicLogs;

    override val id: String = name.toTitleCase().toUnderscoreCase()
}