package world.gregs.voidps.world.activity.skill.mining.ore

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

enum class RegularOre(
    override val xp: Double,
    override val chance: IntRange
) : Ore {
    Clay(5.0, 75..300),
    RuneEssence(5.0, 200..400),
    CopperOre(17.5, 40..500),
    TinOre(17.5, 40..500),
    Limestone(26.5, 25..200),
    BluriteOre(17.5, 30..375),
    IronOre(35.0, 110..350),
    SilverOre(40.0, 25..200),
    PureEssence(5.0, 150..350),
    Coal(50.0, 20..150),
    GoldOre(65.0, 7..75),
    MithrilOre(80.0, 10..75),
    AdamantiteOre(95.0, 5..55),
    RuniteOre(125.0, 1..18);

    override val id: String = name.toTitleCase().toUnderscoreCase()

}