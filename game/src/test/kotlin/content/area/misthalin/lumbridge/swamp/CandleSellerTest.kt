package content.area.misthalin.lumbridge.swamp

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class CandleSellerTest : WorldTest() {

    private fun candleSeller() = createNPC("candle_seller", Tile(3169, 3171))

    private fun customer(coins: Int = 0): Player {
        val player = createPlayer(Tile(3169, 3170))
        if (coins > 0) {
            player.inventory.add("coins", coins)
        }
        return player
    }

    private fun Player.skipToChoice() {
        repeat(20) {
            val id = dialogue ?: error("Dialogue ended before reaching a choice.")
            if (id.startsWith("dialogue_multi")) {
                return
            }
            dialogueContinue()
        }
        error("Did not reach a choice in time.")
    }

    @Test
    fun `Buying after the price explanation still purchases the candle`() {
        val player = customer(coins = 1000)

        player.npcOption(candleSeller(), "Talk-to")
        tick()
        player.skipToChoice()
        player.dialogueOption(2)
        player.skipToChoice()
        player.dialogueOption(1)
        player.dialogueContinue(2)

        assertEquals(0, player.inventory.count("coins"))
        assertEquals(1, player.inventory.count("white_candle_lit"))
    }

    @Test
    fun `Buying after the lantern explanation continues even without coins`() {
        val player = customer()

        player.npcOption(candleSeller(), "Talk-to")
        tick()
        player.skipToChoice()
        player.dialogueOption(2)
        player.skipToChoice()
        player.dialogueOption(3)
        player.skipToChoice()
        player.dialogueOption(1)
        player.dialogueContinue()

        assertEquals(0, player.inventory.count("white_candle_lit"))
        assertNotNull(player.dialogue)
    }
}
