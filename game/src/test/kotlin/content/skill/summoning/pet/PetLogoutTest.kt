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
    fun `pet stats persist as per-pet hunger growth warn vars`() {
        val player = createPlayer(emptyTile)
        player.set("pet_cat_hunger", 4250)
        player.set("pet_cat_growth", 1700)
        player.set("pet_cat_warn", 1)

        val persisted = player.variables.data
        assertEquals(4250, persisted["pet_cat_hunger"])
        assertEquals(1700, persisted["pet_cat_growth"])
        assertEquals(1, persisted["pet_cat_warn"])
        assertEquals(4250, player.getPetHunger("cat"))
        assertEquals(1700, player.getPetGrowth("cat"))
        assertEquals(1, player.getPetWarn("cat"))
    }

    @Test
    fun `incubator state persists per region`() {
        val player = createPlayer(emptyTile)
        player.set("incubator_egg_taverley", "penguin")
        player.set("incubator_end_taverley", 12_345)

        val persisted = player.variables.data
        assertEquals("penguin", persisted["incubator_egg_taverley"])
        assertEquals(12_345, persisted["incubator_end_taverley"])
        assertTrue("incubator_egg_yanille" !in persisted)
    }
}
