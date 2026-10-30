package content.area.misthalin.lumbridge.castle

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class DukeHoracioTest : WorldTest() {

    private fun skipMissing() {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = "true"
        Settings.load(properties)
    }

    @Test
    fun `Duke gives an anti-dragon shield`() {
        skipMissing()
        val player = createPlayer(Tile(3209, 3222, 1))
        val duke = createNPC("duke_horacio", Tile(3210, 3222, 1))

        player.npcOption(duke, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(3)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("anti_dragon_shield"))
    }

    @Test
    fun `Duke doesn't offer a shield before dragon slayer`() {
        val player = createPlayer(Tile(3209, 3222, 1))
        val duke = createNPC("duke_horacio", Tile(3210, 3222, 1))

        player.npcOption(duke, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(3)
        tick()
        player.skipDialogues()

        assertEquals(0, player.inventory.count("anti_dragon_shield"))
    }
}
