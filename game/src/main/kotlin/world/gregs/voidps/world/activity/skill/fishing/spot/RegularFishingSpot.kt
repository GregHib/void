package world.gregs.voidps.world.activity.skill.fishing.spot

import world.gregs.voidps.world.activity.skill.fishing.fish.Catch
import world.gregs.voidps.world.activity.skill.fishing.fish.Junk
import world.gregs.voidps.world.activity.skill.fishing.fish.RegularFish
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait
import world.gregs.voidps.world.activity.skill.fishing.tackle.Tackle

enum class RegularFishingSpot(
    override val tackle: Map<String, List<Triple<Tackle, Bait, Set<Catch>>>>
) : FishingSpot {
    SmallNetBait(
        "Net" to Triple(Tackle.SmallFishingNet, Bait.None, setOf(RegularFish.Shrimp, RegularFish.Anchovies)),
        "Bait" to Triple(Tackle.FishingRod, Bait.FishingBait, setOf(RegularFish.Sardine, RegularFish.Herring))
    ),
    Crayfish(
        "Cage" to Triple(Tackle.CrayfishCage, Bait.None, setOf(RegularFish.Crayfish))
    ),
    LureBait(mapOf(
        "Lure" to listOf(
            Triple(Tackle.FlyFishingRod, Bait.StripyFeathers, setOf(RegularFish.RainbowFish)),
            Triple(Tackle.FlyFishingRod, Bait.Feathers, setOf(RegularFish.Trout, RegularFish.Salmon))
        ),
        "Bait" to listOf(Triple(Tackle.FishingRod, Bait.FishingBait, setOf(RegularFish.Pike)))
    )),
    CageHarpoon(
        "Cage" to Triple(Tackle.LobsterPot, Bait.None, setOf(RegularFish.Lobster)),
        "Harpoon" to Triple(Tackle.Harpoon, Bait.None, setOf(RegularFish.Tuna, RegularFish.Swordfish))
    ),
    BigNetHarpoon(
        "Net" to Triple(Tackle.BigFishingNet, Bait.None, setOf(RegularFish.Mackerel, RegularFish.Cod, RegularFish.Bass, *Junk.values(), RegularFish.BigBass)),
        "Harpoon" to Triple(Tackle.Harpoon, Bait.None, setOf(RegularFish.Shark))
    ),
    SmallNetHarpoon(
        "Net" to Triple(Tackle.SmallFishingNet, Bait.None, setOf(RegularFish.Monkfish)),
        "Harpoon" to Triple(Tackle.Harpoon, Bait.None, setOf(RegularFish.Tuna, RegularFish.Swordfish))
    );

    constructor(vararg pairs: Pair<String, Triple<Tackle, Bait, Set<Catch>>>) : this(pairs.associate { it.first to listOf(it.second) })
}