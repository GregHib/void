package content.skill.fishing

import WorldTest
import equipItem
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CasketTest : WorldTest() {

    @Test
    fun `Opening a casket rewards a little treasure`() {
        val player = createPlayer(Tile(3221, 3218), "casket_open")
        player.inventory.add("casket")

        player.equipItem("casket", option = "Open")
        tick(2)

        assertFalse(player.inventory.contains("casket"))
        assertFalse(player.inventory.isEmpty(), "Expected treasure inside the casket")
        assertTrue(player["messages", emptyList<String>()].any { it.startsWith("You open the casket and find") })
    }
}
