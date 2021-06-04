package world.gregs.voidps.world.activity.skill.fishing.spot

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.fishing.fish.Catch
import world.gregs.voidps.world.activity.skill.fishing.fish.Junk
import world.gregs.voidps.world.activity.skill.fishing.fish.RegularFish
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait
import world.gregs.voidps.world.activity.skill.fishing.tackle.Tackle

enum class RegularFishingSpot(
    override val tackle: Map<String, Triple<List<Tackle>, List<Bait>, List<Catch>>>
) : FishingSpot {
    SmallNetBait(
        "Net" to Triple(listOf(Tackle.SmallFishingNet), listOf(Bait.None), listOf(RegularFish.Shrimps, RegularFish.Anchovies)),
        "Bait" to Triple(listOf(Tackle.FishingRod), listOf(Bait.FishingBait), listOf(RegularFish.Sardine, RegularFish.Herring))
    ),
    Crayfish(
        "Cage" to Triple(listOf(Tackle.CrayfishCage), listOf(Bait.None), listOf(RegularFish.Crayfish))
    ),
    LureBait(
        "Lure" to Triple(listOf(Tackle.FlyFishingRod), listOf(Bait.Feathers, Bait.StripyFeathers), listOf(RegularFish.RainbowFish)),
        "Bait" to Triple(listOf(Tackle.FishingRod), listOf(Bait.FishingBait), listOf(RegularFish.Pike))
    ),
    CageHarpoon(
        "Cage" to Triple(listOf(Tackle.LobsterPot), listOf(Bait.None), listOf(RegularFish.Lobster)),
        "Harpoon" to Triple(listOf(Tackle.Harpoon, Tackle.BarbTailHarpoon), listOf(Bait.None), listOf(RegularFish.Tuna, RegularFish.Swordfish))
    ),
    BigNetHarpoon(
        "Net" to Triple(listOf(Tackle.BigFishingNet), listOf(Bait.None), listOf(RegularFish.Mackerel, RegularFish.Cod, RegularFish.Bass, *Junk.values(), RegularFish.BigBass)),
        "Harpoon" to Triple(listOf(Tackle.Harpoon, Tackle.BarbTailHarpoon), listOf(Bait.None), listOf(RegularFish.Shark))
    ),
    SmallNetHarpoon(
        "Net" to Triple(listOf(Tackle.SmallFishingNet), listOf(Bait.None), listOf(RegularFish.Monkfish)),
        "Harpoon" to Triple(listOf(Tackle.Harpoon, Tackle.BarbTailHarpoon), listOf(Bait.None), listOf(RegularFish.Tuna, RegularFish.Swordfish))
    );

    override val id: String = name.toTitleCase().toUnderscoreCase()

    constructor(vararg pairs: Pair<String, Triple<List<Tackle>, List<Bait>, List<Catch>>>) : this(pairs.associate { it.first to it.second })
}