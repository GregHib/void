package content.area.asgarnia.port_sarim

import WorldTest
import containsMessage
import dialogueOption
import equipItem
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeamanTest : WorldTest() {

    @Test
    fun `A ring of charos gets you aboard for free`() {
        val player = createPlayer(Tile(3028, 3217))
        player.inventory.add("ring_of_charos_a")
        player.equipItem("ring_of_charos_a", option = "Wear")
        tick(2)
        val tobias = createNPC("captain_tobias_2", Tile(3028, 3216))
        tick(2)

        player.npcOption(tobias, "Talk-to")
        chat(player)
        player.dialogueOption("line2") // Or I could pay you nothing at all...
        chat(player)

        assertTrue(player.containsMessage("You board the ship."))
        assertFalse(player.containsMessage("You pay 30 coins and board the ship."))
    }

    @Test
    fun `Without the ring the free option isn't offered`() {
        val player = createPlayer(Tile(3028, 3217))
        player.inventory.add("coins", 30)
        val tobias = createNPC("captain_tobias_2", Tile(3028, 3216))
        tick(2)

        player.npcOption(tobias, "Talk-to")
        chat(player)
        player.dialogueOption("line2") // No, thank you.
        chat(player)

        assertFalse(player.containsMessage("You board the ship."))
        assertEquals(30, player.inventory.count("coins"))
    }

    private fun chat(player: Player) {
        tickIf(60) { player.dialogue == null }
        player.skipDialogues()
    }
}
