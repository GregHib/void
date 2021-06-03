package world.gregs.voidps.world.activity.skill.fishing.fish

import world.gregs.voidps.world.activity.skill.Id

interface Catch : Id {
    val level: Int
    val xp: Double
    val chance: IntRange
}