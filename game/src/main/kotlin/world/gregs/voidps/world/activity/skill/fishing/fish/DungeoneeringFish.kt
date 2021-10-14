package world.gregs.voidps.world.activity.skill.fishing.fish

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

enum class DungeoneeringFish(
    override val level: Int,
    override val xp: Double,
    override val chance: IntRange = 1..1
) : Catch {
    HeimCrab(1, 9.0),
    Redeye(10, 27.0),
    DuskEel(20, 45.0),
    GiantFlatfish(30, 63.0),
    ShortFinnedEel(40, 81.0),
    WebSnipper(50, 99.0),
    Bouldabass(60, 117.0),
    SalveEel(70, 135.0),
    BlueCrab(80, 153.0),
    CaveMoray(90, 171.0),
    VileFish(0, 255.0);

    override val id: String = name.toTitleCase().toUnderscoreCase()
}