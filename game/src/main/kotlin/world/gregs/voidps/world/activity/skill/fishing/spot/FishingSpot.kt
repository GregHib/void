package world.gregs.voidps.world.activity.skill.fishing.spot

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.world.activity.skill.fishing.fish.Catch
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait
import world.gregs.voidps.world.activity.skill.fishing.tackle.Tackle

interface FishingSpot {
    val tackle: Map<String, List<Triple<Tackle, Bait, Set<Catch>>>>

    companion object {
        private val spots = listOf(
            *RegularFishingSpot.values()
        )

        fun get(npc: NPC): FishingSpot? {
            return spots.firstOrNull { npc.name.startsWith(it.name) }
        }
    }
}