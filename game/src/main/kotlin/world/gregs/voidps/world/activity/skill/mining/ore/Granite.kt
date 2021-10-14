package world.gregs.voidps.world.activity.skill.mining.ore

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

enum class Granite(
    override val xp: Double,
    override val chance: IntRange
) : Ore {
    Granite500g(50.0, 16..100),
    Granite2kg(60.0, 8..75),
    Granite5kg(75.0, 6..64);

    override val id: String = name.toTitleCase().toUnderscoreCase()

}