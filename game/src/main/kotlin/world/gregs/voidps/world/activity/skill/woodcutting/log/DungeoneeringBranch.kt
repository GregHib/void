package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

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