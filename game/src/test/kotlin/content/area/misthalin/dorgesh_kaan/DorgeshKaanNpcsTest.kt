package content.area.misthalin.dorgesh_kaan

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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

class DorgeshKaanNpcsTest : WorldTest() {

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
    fun `Tegdak sells back a lost ancient mace`() {
        skipMissing(true)
        val player = createPlayer(Tile(2744, 5343))
        player.inventory.add("coins", 1000)
        val tegdak = createNPC("tegdak_dorgesh_kaan", Tile(2744, 5344))
        assertEquals("tegdak_dorgesh_kaan_station_construction_site", tegdak.def(player).stringId)

        player.npcOption(tegdak, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(2)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()

        assertTrue(player.inventory.contains("ancient_mace"))
        assertFalse(player.inventory.contains("coins"))
    }

    @Test
    fun `Tegdak keeps the mace without the coins`() {
        skipMissing(true)
        val player = createPlayer(Tile(2744, 5343))
        val tegdak = createNPC("tegdak_dorgesh_kaan", Tile(2744, 5344))

        player.npcOption(tegdak, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(2)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()

        assertFalse(player.inventory.contains("ancient_mace"))
    }

    @Test
    fun `Ambassador Alvijar talks after Another Slice of H A M`() {
        skipMissing(true)
        val player = createPlayer(Tile(2737, 5350, 1))
        val alvijar = createNPC("ambassador_alvijar_dorgesh_kaan_2", Tile(2737, 5351, 1))
        assertEquals("ambassador_alvijar_dorgesh_kaan_3", alvijar.def(player).stringId)

        player.npcOption(alvijar, "Talk-to")
        tick(1)
        assertNotNull(player.dialogue)
        player.skipDialogues()

        assertNull(player.dialogue)
    }

    @Test
    fun `Zanik talks after The Chosen Commander`() {
        skipMissing(true)
        val player = createPlayer(Tile(2745, 5352, 1))
        val zanik = createNPC("zanik_dorgesh_kaan_5", Tile(2745, 5353, 1))
        assertEquals("zanik_dorgesh_kaan_5_2", zanik.def(player).stringId)

        player.npcOption(zanik, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertNotNull(player.dialogue)
        player.dialogueOption(1)
        player.skipDialogues()

        assertNull(player.dialogue)
    }
}
