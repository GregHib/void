package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

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