package content.area.morytania.port_phasmatys

import WorldTest
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class BillTeachTest : WorldTest() {

    private fun skipMissing(skip: Boolean) {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = skip.toString()
        Settings.load(properties)
        SettingsReload.now()
    }

    @AfterEach
    fun reset() {
        skipMissing(false)
    }

    @Test
    fun `Bill Teach replaces a lost book o' piracy`() {
        skipMissing(true)
        val player = createPlayer(Tile(3713, 3498, 1))
        val bill = createNPC("bill_teach_port_phasmatys", Tile(3713, 3497, 1))
        assertEquals("bill_teach_port_phasmatys_2", bill.def(player).stringId)

        player.npcOption(bill, "Talk-to")
        tick(1)
        assertNotNull(player.dialogue)
        player.skipDialogues()

        assertTrue(player.inventory.contains("little_book_o_piracy"))
    }

    @Test
    fun `Bill Teach offers a lift when the book is owned`() {
        skipMissing(true)
        val player = createPlayer(Tile(3713, 3498, 1))
        player.inventory.add("little_book_o_piracy")
        val bill = createNPC("bill_teach_port_phasmatys", Tile(3713, 3497, 1))

        player.npcOption(bill, "Talk-to")
        tick(1)
        assertNotNull(player.dialogue)
        player.skipDialogues()

        assertEquals(1, player.inventory.count("little_book_o_piracy"))
    }

    @Test
    fun `Travel sails from Port Phasmatys to Mos Le'Harmless`() {
        skipMissing(true)
        val player = createPlayer(Tile(3713, 3498, 1))
        val bill = createNPC("bill_teach_port_phasmatys", Tile(3713, 3497, 1))

        player.npcOption(bill, "Travel")
        tick(8)

        assertEquals(Tile(3682, 2948, 1), player.tile)
    }

    @Test
    fun `Travel sails from Mos Le'Harmless to Port Phasmatys`() {
        skipMissing(true)
        val player = createPlayer(Tile(3682, 2948, 1))
        val bill = createNPC("bill_teach_port_phasmatys", Tile(3682, 2949, 1))

        player.npcOption(bill, "Travel")
        tick(8)

        assertEquals(Tile(3714, 3499, 1), player.tile)
    }
}
