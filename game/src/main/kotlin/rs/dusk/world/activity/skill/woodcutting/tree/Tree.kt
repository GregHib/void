package rs.dusk.world.activity.skill.woodcutting.tree

import rs.dusk.world.activity.skill.Id
import rs.dusk.world.activity.skill.woodcutting.log.Log

interface Tree : Id {
    val log: Log?
    val level: Int
    val xp: Double
    val fellRate: Double
    val chance: IntRange
    val lowDifference: IntRange
    val highDifference: IntRange
}