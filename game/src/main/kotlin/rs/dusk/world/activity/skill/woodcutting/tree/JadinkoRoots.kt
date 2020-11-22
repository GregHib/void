package rs.dusk.world.activity.skill.woodcutting.tree

import rs.dusk.world.activity.skill.woodcutting.log.JadinkoRoot

@Suppress("EnumEntryName")
enum class JadinkoRoots(
    override val log: JadinkoRoot,
    override val level: Int,
    override val xp: Double,
    override val depleteRate: Double,
    override val chance: IntRange,
    override val lowDifference: IntRange,
    override val highDifference: IntRange,
    override val respawnDelay: IntRange
) : Tree {
    Straight_Jade_Roots(JadinkoRoot.Straight_Root, 83, 80.5, 1.00, 0..0, 0..0, 0..0, 0..0),
    Curly_Jade_Roots(JadinkoRoot.Curly_Root, 83, 80.8, 1.00, 0..0, 0..0, 0..0, 0..0);

    override val id: String = name.toLowerCase()
}