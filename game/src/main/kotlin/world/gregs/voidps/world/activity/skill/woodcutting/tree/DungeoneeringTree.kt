package world.gregs.voidps.world.activity.skill.woodcutting.tree

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.woodcutting.log.DungeoneeringBranch

enum class DungeoneeringTree(
    override val log: DungeoneeringBranch,
    override val level: Int,
    override val xp: Double,
    override val depleteRate: Double,
    override val chance: IntRange,
    override val lowDifference: IntRange,
    override val highDifference: IntRange,
    override val respawnDelay: IntRange
) : Tree {
    TangleGumTree(DungeoneeringBranch.TangleGumBranches, 1, 35.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    SeepingElmTree(DungeoneeringBranch.SeepingElmBranches, 10, 60.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    BloodSpindleTree(DungeoneeringBranch.BloodSpindleBranches, 20, 85.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    UtukuTree(DungeoneeringBranch.UtukuBranches, 30, 115.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    SpinebeamTree(DungeoneeringBranch.SpinebeamBranches, 40, 145.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    BovistranglerTree(DungeoneeringBranch.BovistranglerBranches, 50, 175.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    ThigatTree(DungeoneeringBranch.ThigatBranches, 60, 210.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    CorpsethornTree(DungeoneeringBranch.CorpsethornBranches, 70, 245.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    EntgallowTree(DungeoneeringBranch.EntgallowBranches, 80, 285.0, 0.125, 0..0, 0..0, 0..0, -1..-1),
    GravecreeperTree(DungeoneeringBranch.GravecreeperBranches, 90, 330.0, 0.125, 0..0, 0..0, 0..0, -1..-1);

    override val id: String = name.toTitleCase().toUnderscoreCase()
}