package content.skill.fishing

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import FakeRandom
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import WorldTest
import npcOption

internal class FishingTest : WorldTest() {

    @Test
    fun `Fishing gives fish and removes bait`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Fishing, 20)
        val fishingSpot = createNPC("fishing_spot_lure_bait_lumbridge", emptyTile.addY(1))
        player.inventory.add("fly_fishing_rod")
        player.inventory.add("feather", 100)

        player.npcOption(fishingSpot, "Lure")
        tickIf { player.inventory.spaces >= 26 }

        assertTrue(player.inventory.count("feather") < 100)
        assertTrue(player.inventory.contains("raw_trout"))
        assertTrue(player.experience.get(Skill.Fishing) > 0)
    }


}