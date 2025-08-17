package content.social.trade.exchange

import WorldTest
import dialogueContinue
import dialogueOption
import interfaceOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GrandExchangeTutorialTest : WorldTest() {

    @Test
    fun `Grand exchange tutorial`() {
        val player = createPlayer(Tile(3168, 3476))
        val brugsen = createNPC("brugsen_bursen", Tile(3168, 3475))

        player.npcOption(brugsen, "Talk-to")
        tick()

        player.dialogueContinue(3)
        player.dialogueOption("line1")
        player.dialogueContinue(7)
        assertEquals("exchange_offers_tutorial", player.menu)
        player.interfaceOption("exchange_offers_tutorial", "continue", "Continue")
        player.interfaceOption("exchange_offers_tutorial", "continue", "Continue")
        assertEquals("exchange_buy_tutorial", player.menu)
        player.interfaceOption("exchange_buy_tutorial", "continue", "Continue")
        assertEquals("exchange_confirm_tutorial", player.menu)
        player.interfaceOption("exchange_confirm_tutorial", "continue", "Continue")
        assertEquals("exchange_offers_tutorial", player.menu)
        player.interfaceOption("exchange_offers_tutorial", "continue", "Continue")
        assertNull(player.menu)
        player.dialogueContinue(3)
        assertEquals("exchange_wait_tutorial", player.menu)
        player.interfaceOption("exchange_wait_tutorial", "continue", "Continue")
        assertNull(player.menu)
        player.dialogueContinue(5)
        player.dialogueOption("line3")
        player.dialogueContinue()
        player.dialogueOption("line1")
        player.dialogueContinue(2)
        assertEquals("common_item_costs", player.menu)
    }
}
