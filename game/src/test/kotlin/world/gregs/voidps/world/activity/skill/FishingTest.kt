package world.gregs.voidps.world.activity.skill

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.npcOption

internal class FishingTest : WorldMock() {

    @Test
    fun `Fishing gives fish and removes bait`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("fisher")
        player.levels.setOffset(Skill.Fishing, 100)
        val fishingSpot = createNPC("fishing_spot_lure_bait", Tile(100, 101))
        player.inventory.add("fly_fishing_rod")
        player.inventory.add("feather", 100)

        player.npcOption(fishingSpot, "Lure")
        tickIf { player.inventory.spaces >= 26 }

        assertTrue(player.inventory.getCount("feather") < 100)
        assertTrue(player.inventory.contains("raw_trout"))
        assertTrue(player.experience.get(Skill.Fishing) > 0)
    }


}