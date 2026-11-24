package content.entity.npc

import WorldTest
import dialogueOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.type.Tile

class BankerTest : WorldTest() {

    @Test
    fun `Lunar isle banker opens the bank`() {
        val player = createPlayer(Tile(2097, 3919))
        val banker = createNPC("sirsal_banker", Tile(2097, 3921))

        player.npcOption(banker, "Bank")
        tick(2)

        assertTrue(player.hasOpen("bank"))
    }

    @Test
    fun `Bank booth talks to the nearest banker`() {
        val player = createPlayer(Tile(2098, 3919))
        createNPC("sirsal_banker", Tile(2098, 3921))
        val booth = createObject("bank_booth_lunar_isle", Tile(2098, 3920))

        player.objectOption(booth, "Use")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick(2)

        assertTrue(player.hasOpen("bank"))
    }
}
