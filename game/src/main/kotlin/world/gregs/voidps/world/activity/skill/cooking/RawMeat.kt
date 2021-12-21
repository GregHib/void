package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

enum class RawMeat(
    override val level: Int,
    override val xp: Double,
    override val fireChance: IntRange,
    override val rangeChance: IntRange = fireChance,
    val cooksRangeChance: IntRange = rangeChance,
    val gauntletChance: IntRange = rangeChance,
) : Raw {
    RawBeef(1, 30.0, 128..512, cooksRangeChance = 138..532),
    RawBearMeat(1, 30.0, 128..512, cooksRangeChance = 138..532),
    RawBoarMeat(1, 30.0, 128..512, cooksRangeChance = 138..532),
    RawRatMeat(1, 30.0, 128..512, cooksRangeChance = 138..532),
    RawYakMeat(1, 40.0, 128..512, cooksRangeChance = 138..532),
    RawPawyaMeat(1, 30.0, 128..512, cooksRangeChance = 138..532),
//    Sinew(1, 3.0, 128..512),
    RawChicken(1, 30.0, 0..0),

    ;

    override val id: String = name.toTitleCase().toUnderscoreCase()
}