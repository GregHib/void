package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase

enum class DungeoneeringBranch : Log {
    TangleGumBranches,
    SeepingElmBranches,
    BloodSpindleBranches,
    UtukuBranches,
    SpinebeamBranches,
    BovistranglerBranches,
    ThigatBranches,
    CorpsethornBranches,
    EntgallowBranches,
    GravecreeperBranches;

    override val id: String = name.toTitleCase().toUnderscoreCase()
}