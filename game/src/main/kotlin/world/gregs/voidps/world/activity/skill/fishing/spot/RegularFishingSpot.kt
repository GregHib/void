package world.gregs.voidps.world.activity.skill.fishing.spot

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.fishing.fish.Catch
import world.gregs.voidps.world.activity.skill.fishing.fish.Junk
import world.gregs.voidps.world.activity.skill.fishing.fish.RegularFish
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait
import world.gregs.voidps.world.activity.skill.fishing.tackle.Tackle

enum class RegularFishingSpot(
    override val tackle: Map<String, Pair<List<Tackle>, Map<Bait, List<Catch>>>>
) : FishingSpot {
    SmallNetBait(
        "Net" to Pair(listOf(Tackle.SmallFishingNet), mapOf(Bait.None to listOf(RegularFish.Shrimps, RegularFish.Anchovies))),
        "Bait" to Pair(listOf(Tackle.FishingRod), mapOf(Bait.FishingBait to listOf(RegularFish.Sardine, RegularFish.Herring)))
    ),
    Crayfish(
        "Cage" to Pair(listOf(Tackle.CrayfishCage), mapOf(Bait.None to listOf(RegularFish.Crayfish)))
    ),
    LureBait(
        "Lure" to Pair(listOf(Tackle.FlyFishingRod), linkedMapOf(
            Bait.Feather to listOf(RegularFish.Trout, RegularFish.Salmon),
            Bait.StripyFeather to listOf(RegularFish.RainbowFish)
        )),
        "Bait" to Pair(listOf(Tackle.FishingRod), mapOf(Bait.FishingBait to listOf(RegularFish.Pike)))
    ),
    CageHarpoon(
        "Cage" to Pair(listOf(Tackle.LobsterPot), mapOf(Bait.None to listOf(RegularFish.Lobster))),
        "Harpoon" to Pair(listOf(Tackle.Harpoon, Tackle.BarbTailHarpoon), mapOf(Bait.None to listOf(RegularFish.Tuna, RegularFish.Swordfish)))
    ),
    BigNetHarpoon(
        "Net" to Pair(listOf(Tackle.BigFishingNet), mapOf(Bait.None to listOf(RegularFish.Mackerel, RegularFish.Cod, RegularFish.Bass, *Junk.values(), RegularFish.BigBass))),
        "Harpoon" to Pair(listOf(Tackle.Harpoon, Tackle.BarbTailHarpoon), mapOf(Bait.None to listOf(RegularFish.Shark)))
    ),
    SmallNetHarpoon(
        "Net" to Pair(listOf(Tackle.SmallFishingNet), mapOf(Bait.None to listOf(RegularFish.Monkfish))),
        "Harpoon" to Pair(listOf(Tackle.Harpoon, Tackle.BarbTailHarpoon), mapOf(Bait.None to listOf(RegularFish.Tuna, RegularFish.Swordfish)))
    );

    override val id: String = name.toTitleCase().toUnderscoreCase()

    constructor(vararg pairs: Pair<String, Pair<List<Tackle>, Map<Bait, List<Catch>>>>) : this(pairs.toMap())
}