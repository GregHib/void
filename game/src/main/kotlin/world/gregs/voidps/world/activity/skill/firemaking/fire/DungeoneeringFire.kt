package world.gregs.voidps.world.activity.skill.firemaking.fire

import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.woodcutting.log.DungeoneeringBranch

enum class DungeoneeringFire(
    override val log: DungeoneeringBranch,
    override val level: Int,
    override val xp: Double,
    override val life: Int
) : Fire {
    TangleGumBranches(DungeoneeringBranch.TangleGumBranches, 1, 25.0, 60),
    SeepingElmBranches(DungeoneeringBranch.SeepingElmBranches, 10, 44.5, 75),
    BloodSpindleBranches(DungeoneeringBranch.BloodSpindleBranches, 20, 65.6, 82),
    UtukuBranches(DungeoneeringBranch.UtukuBranches, 30, 88.3, 90),
    SpinebeamBranches(DungeoneeringBranch.SpinebeamBranches, 40, 112.6, 100),
    BovistranglerBranches(DungeoneeringBranch.BovistranglerBranches, 50, 138.5, 120),
    ThigatBranches(DungeoneeringBranch.ThigatBranches, 60, 166.0, 140),
    CorpsethornBranches(DungeoneeringBranch.CorpsethornBranches, 70, 195.1, 170),
    EntgallowBranches(DungeoneeringBranch.EntgallowBranches, 80, 225.8, 185),
    GraveCreeperBranches(DungeoneeringBranch.GraveCreeperBranches, 90, 258.1, 200);

    override val id: String = name.toUnderscoreCase()

}