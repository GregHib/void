package world.gregs.voidps.world.activity.skill.fishing.tackle

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.Id

enum class Tackle : Id {
    SmallFishingNet,
    FishingRod,
    CrayfishCage,
    FlyFishingRod,
    LobsterPot,
    Harpoon,
    BigFishingNet,
    BarbarianRod;

    override val id: String = name.toTitleCase().toUnderscoreCase()

}