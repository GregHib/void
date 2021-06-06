package world.gregs.voidps.world.activity.skill.mining.ore

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase

enum class Gemstone(
    override val xp: Double,
    override val chance: IntRange
) : Ore {
    UncutOpal(65.0, 120..120),
    UncutJade(65.0, 60..60),
    UncutRedTopaz(65.0, 30..30),
    UncutSapphire(65.0, 18..18),
    UncutEmerald(65.0, 10..10),
    UncutRuby(65.0, 10..10),
    UncutDiamond(65.0, 8..8);

    override val id: String = name.toTitleCase().toUnderscoreCase()

}