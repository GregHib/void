package content.skill.summoning.pet

import WorldTest
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

/**
 * Round-trips pet state through the persistent variable store. The values that
 * matter end up in `player.variables.data` (the map that gets written to disk
 * by `PlayerSave.copy()`), not the session-only `temp` map.
 */
internal class PetLogoutTest : WorldTest() {

    @Test
    fun `pet_active_item is in the persisted variable map after summon`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("pet_kitten")
        player.itemOption("Drop", "pet_kitten")
        tick(3)

        val persisted = player.variables.data
        assertEquals("pet_kitten", persisted["pet_active_item"])
    }

    @Test
    fun `pet_stats blob encodes hunger growth warn and persists`() {
        val player = createPlayer(emptyTile)
        player.updatePetStats("cat") {
            hunger = 42.5
            growth = 17.0
            warn = 1
        }

        val blob = player.variables.data["pet_stats"] as? String
        assertEquals("cat:42.5:17.0:1", blob)
        assertEquals(42.5, player.getPetHunger("cat"))
        assertEquals(17.0, player.getPetGrowth("cat"))
        assertEquals(1, player.getPetWarn("cat"))
    }

    @Test
    fun `incubator state persists per region`() {
        val player = createPlayer(emptyTile)
        player.set("incubator_egg_taverley", "penguin")
        player.set("incubator_end_taverley", 1_700_000_000_000L)

        val persisted = player.variables.data
        assertEquals("penguin", persisted["incubator_egg_taverley"])
        assertEquals(1_700_000_000_000L, persisted["incubator_end_taverley"])
        assertTrue("incubator_egg_yanille" !in persisted)
    }
}
