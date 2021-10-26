package world.gregs.voidps.world.activity.skill.fishing.spot

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.world.activity.skill.Id
import world.gregs.voidps.world.activity.skill.fishing.fish.Catch
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait
import world.gregs.voidps.world.activity.skill.fishing.tackle.Tackle

interface FishingSpot : Id {
    val tackle: Map<String, Pair<List<Tackle>, Map<Bait, List<Catch>>>>

    companion object {
        private val spots = listOf(
            *RegularFishingSpot.values()
        )

        fun get(npc: NPC): FishingSpot? {
            return spots.firstOrNull { npc.id.startsWith("fishing_spot_${it.id}") }
        }
    }
}