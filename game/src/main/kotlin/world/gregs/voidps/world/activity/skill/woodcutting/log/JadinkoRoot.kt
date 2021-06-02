package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase

enum class JadinkoRoot : Log {
    StraightRoot,
    CurlyRoot;

    override val id: String = name.toTitleCase().toUnderscoreCase()
}