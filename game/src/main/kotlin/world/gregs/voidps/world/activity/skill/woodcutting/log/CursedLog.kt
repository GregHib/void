package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase

enum class CursedLog : Log {
    CursedWillowLogs,
    CursedMagicLogs;

    override val id: String = name.toTitleCase().toUnderscoreCase()
}