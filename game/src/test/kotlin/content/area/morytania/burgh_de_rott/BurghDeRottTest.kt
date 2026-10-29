package content.area.morytania.burgh_de_rott

import WorldTest
import npcOption
import objectOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class BurghDeRottTest : WorldTest() {

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
    fun `Repaired bank booth opens the banker menu`() {
        skipMissing(true)
        val player = createPlayer(Tile(3494, 3210))
        createNPC("banker", Tile(3494, 3212))
        val booth = createObject("bank_booth_burgh_de_rott_base", Tile(3494, 3211))
        assertEquals("bank_booth_burgh_de_rott", booth.def(player).stringId)

        player.objectOption(booth, "Use")
        tick(3)

        assertTrue(player.hasOpen("dialogue_multi5"))
    }

    @Test
    fun `Repaired furnace smelts`() {
        skipMissing(true)
        val player = createPlayer(Tile(3526, 3210))
        player.inventory.add("copper_ore")
        player.inventory.add("tin_ore")
        val furnace = createObject("furnace_burgh_de_rott_base", Tile(3527, 3209))
        assertEquals("furnace_burgh_de_rott", furnace.def(player).stringId)

        player.objectOption(furnace, "Smelt")
        tick(3)

        assertTrue(player.hasOpen("dialogue_skill_creation"))
    }

    @Test
    fun `Cornelius banks for players once the bank is rebuilt`() {
        skipMissing(true)
        val player = createPlayer(Tile(3496, 3210))
        val cornelius = createNPC("cornelius_2", Tile(3496, 3212))
        assertEquals("cornelius_2_2", cornelius.def(player).stringId)

        player.npcOption(cornelius, "Talk-to")
        tick(3)
        player.skipDialogues()

        assertTrue(player.hasOpen("dialogue_multi5"))
    }

    @Test
    fun `Burgh de Rott stays broken when not skipping`() {
        skipMissing(false)
        val player = createPlayer(Tile(3526, 3210))
        val furnace = createObject("furnace_burgh_de_rott_base", Tile(3527, 3209))

        assertEquals("12806", furnace.def(player).stringId.ifEmpty { furnace.def(player).id.toString() })
    }
}
