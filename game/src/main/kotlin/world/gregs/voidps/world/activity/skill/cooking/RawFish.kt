package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

enum class RawFish(
    override val level: Int,
    override val xp: Double,
    override val fireChance: IntRange,
    override val rangeChance: IntRange = fireChance,
    val cooksRangeChance: IntRange = rangeChance,
    val gauntletChance: IntRange = rangeChance,
) : Raw {
    RawCrayfish(1, 30.0, 128..512),
    RawShrimps(1, 30.0, 128..512, cooksRangeChance = 138..532),
    RawAnchovies(1, 30.0, 128..512, cooksRangeChance = 138..532),
    RawSardine(1, 40.0, 118..492, cooksRangeChance = 128..512),
    RawKarambwan(1, 80.0, 70..255),
    RawHerring(5, 50.0, 108..472, cooksRangeChance = 118..492),
    RawMackerel(10, 60.0, 98..452, cooksRangeChance = 108..472),
    RawTrout(15, 70.0, 88..432, cooksRangeChance = 98..452),
    RawCod(18, 75.0, 83..422, 88..432, 93..442),
    RawPike(20, 80.0, 78..412, cooksRangeChance = 88..432),
    RawSalmon(25, 90.0, 68..392, cooksRangeChance = 78..402),
    RawTuna(30, 100.0, 58..372),
//    RawKarambwan(30, 190.0, 70..255),
    RawRainbowFish(35, 110.0, 56..370),
    RawLobster(40, 120.0, 38..332, gauntletChance = 55..368),
    RawBass(43, 130.0, 33..312),
    RawSwordFish(45, 140.0, 18..292, 30..310),
    RawMonkfish(62, 150.0, 11..275, 13..280, gauntletChance = 24..290),
    RawShark(80, 210.0, 1..202, 1..232, gauntletChance = 15..270),
    RawCavefish(88, 214.0, 4..230, gauntletChance = 12..269),
    RawMantaRay(91, 216.3, 1..202, 1..222),
    RawRocktail(92, 225.0, 1..180, gauntletChance = 1..269),
    ;

    override val id: String = name.toTitleCase().toUnderscoreCase()
}