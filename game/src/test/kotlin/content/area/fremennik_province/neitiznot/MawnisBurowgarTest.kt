package content.area.fremennik_province.neitiznot

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class MawnisBurowgarTest : WorldTest() {

    private fun skipMissing() {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = "true"
        Settings.load(properties)
    }

    @Test
    fun `Buy a helm of neitiznot`() {
        skipMissing()
        val player = createPlayer(Tile(2335, 3799))
        player.inventory.add("coins", 50_000)
        val mawnis = createNPC("mawnis_burowgar_neitiznot", Tile(2335, 3798))

        player.npcOption(mawnis, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("helm_of_neitiznot"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Can't buy a helm without enough coins`() {
        skipMissing()
        val player = createPlayer(Tile(2335, 3799))
        player.inventory.add("coins", 49_999)
        val mawnis = createNPC("mawnis_burowgar_neitiznot", Tile(2335, 3798))

        player.npcOption(mawnis, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()

        assertEquals(0, player.inventory.count("helm_of_neitiznot"))
        assertEquals(49_999, player.inventory.count("coins"))
    }
}
