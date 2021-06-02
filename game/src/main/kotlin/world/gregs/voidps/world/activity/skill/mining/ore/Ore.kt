package world.gregs.voidps.world.activity.skill.mining.ore

import world.gregs.voidps.world.activity.skill.Id

interface Ore : Id {
    val xp: Double
    val chance: IntRange
}