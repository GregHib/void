package world.gregs.voidps.world.activity.skill.mining.ore

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

enum class Sandstone(
    override val xp: Double,
    override val chance: IntRange
) : Ore {
    Sandstone1kg(30.0, 25..200),
    Sandstone2kg(40.0, 16..100),
    Sandstone5kg(50.0, 8..75),
    Sandstone10kg(60.0, 4..50);

    override val id: String = name.toTitleCase().toUnderscoreCase()

}