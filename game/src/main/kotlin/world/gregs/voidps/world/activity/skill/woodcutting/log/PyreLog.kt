package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase

enum class PyreLog : Log {
    PyreLogs,
    OakPyreLogs,
    WillowPyreLogs,
    TeakPyreLogs,
    ArcticPyreLogs,
    MaplePyreLogs,
    MahoganyPyreLogs,
    EucalyptusPyreLogs,
    YewPyreLogs,
    MagicPyreLogs;

    override val id: String = name.toTitleCase().toUnderscoreCase()
}