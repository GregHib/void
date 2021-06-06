package world.gregs.voidps.world.activity.skill.fishing.tackle

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.Id

enum class Bait : Id {
    None,
    FishingBait,
    Feather,
    StripyFeather,
    FishOffcuts,
    Roe,
    Caviar,
    LivingMinerals;

    override val id: String = name.toTitleCase().toUnderscoreCase()

}