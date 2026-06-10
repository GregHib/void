package content.skill.summoning.pet

import WorldTest
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class PetDropTest : WorldTest() {

    @Test
    fun `Drop on pet kitten spawns follower NPC and removes item`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("pet_kitten")
        assertTrue(player.inventory.contains("pet_kitten"))

        player.itemOption("Drop", "pet_kitten")
        tick(3)

        assertFalse(player.inventory.contains("pet_kitten"), "kitten item should leave inventory")
        assertEquals("pet_kitten", player.get("pet_active_item", ""), "pet_active_item should be set")
    }
}
