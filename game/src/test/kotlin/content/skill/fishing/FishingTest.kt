package content.skill.fishing

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom

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

    @Test
    fun `Higher level fish are rolled before lower level fish`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Fishing, 99)
        val fishingSpot = createNPC("fishing_spot_tai_bwo_wannai", emptyTile.addY(1))
        player.inventory.add("small_fishing_net")

        player.npcOption(fishingSpot, "Net")
        tickIf { player.inventory.spaces >= 27 }

        assertTrue(player.inventory.contains("raw_karambwanji"))
        assertFalse(player.inventory.contains("raw_shrimps"))
    }

    @Test
    fun `Bait is only used for fish the player can catch`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Fishing, 25)
        val fishingSpot = createNPC("fishing_spot_lure_bait_lumbridge", emptyTile.addY(1))
        player.inventory.add("fly_fishing_rod")
        player.inventory.add("feather", 100)
        player.inventory.add("stripy_feather", 100)

        player.npcOption(fishingSpot, "Lure")
        tickIf { player.inventory.spaces >= 25 }

        assertTrue(player.inventory.contains("raw_trout"))
        assertEquals(100, player.inventory.count("stripy_feather"))
    }

    @Test
    fun `Fishing spot catches are listed highest level first`() {
        for (spot in Tables.get("fishing_spots").rows()) {
            for (option in listOf("net", "bait", "lure", "cage", "harpoon")) {
                val fish = Tables.itemListOrNull("${spot.stringId}.$option") ?: continue
                val levels = fish.map { Tables.intOrNull("fishing.$it.level") ?: 0 }
                assertEquals(levels.sortedDescending(), levels, "${spot.rowId} $option $fish")
            }
        }
    }
}
