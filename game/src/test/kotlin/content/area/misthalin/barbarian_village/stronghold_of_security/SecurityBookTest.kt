package content.area.misthalin.barbarian_village.stronghold_of_security

import WorldTest
import itemOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecurityBookTest : WorldTest() {

    @Test
    fun `Reading the security book opens the indexed book interface`() {
        val player = createPlayer(Tile(3087, 3496))
        player.inventory.add("security_book")

        player.itemOption("Read", "security_book")
        tick(2)

        assertTrue(player.hasOpen("book_indexed"))
        assertEquals("book_indexed", player.interfaces.get("main_screen"))
        assertEquals("security_book", player["book", ""])
        assertEquals(0, player["book_page", -1])
    }
}
