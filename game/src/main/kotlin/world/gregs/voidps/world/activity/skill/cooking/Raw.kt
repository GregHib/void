package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.world.activity.skill.Id

interface Raw : Id {
    val level: Int
    val xp: Double
    val fireChance: IntRange
    val rangeChance: IntRange
}