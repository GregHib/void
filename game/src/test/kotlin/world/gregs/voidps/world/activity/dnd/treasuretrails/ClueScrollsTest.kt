package world.gregs.voidps.world.activity.dnd.treasuretrails

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.script.WorldTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClueScrollsTest : WorldTest() {

    @Test
    fun `Owning a clue scroll sets variable`() {
        val player = createPlayer("player")
        player.inventory.add("clue_scroll_hard")
        assertTrue(player["hard_clue", false])
    }

    @Test
    fun `Transferring a clue scroll into bank doesn't remove variable`() {
        val player = createPlayer("player")
        player.inventory.add("clue_scroll_hard")
        assertTrue(player["hard_clue", false])

        val success = player.inventory.transaction {
            move(0, player.bank)
        }
        assertTrue(success)
        assertTrue(player["hard_clue", false])
    }

    @Test
    fun `Dropping clue scroll removes variable`() {
        val player = createPlayer("player")
        player.inventory.add("clue_scroll_hard")
        assertTrue(player["hard_clue", false])
        player.inventory.remove("clue_scroll_hard")
        assertFalse(player["hard_clue", false])
    }
}