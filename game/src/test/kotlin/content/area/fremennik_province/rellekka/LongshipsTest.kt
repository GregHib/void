package content.area.fremennik_province.rellekka

import WorldTest
import containsMessage
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class LongshipsTest : WorldTest() {

    private fun skipMissing() {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = "true"
        Settings.load(properties)
    }

    @Test
    fun `Sail to Jatizso and back`() {
        skipMissing()
        val player = createPlayer(Tile(2643, 3709))
        val mord = createNPC("mord_gunnars_rellekka", Tile(2644, 3709))

        player.npcOption(mord, "Travel-Jatizso")
        tick(18)
        assertEquals(Tile(2421, 3781), player.tile)
        assertTrue(player.containsMessage("The ship arrives at Jatizso."))

        val mordJatizso = createNPC("mord_gunnars_jatizo", Tile(2421, 3782))
        player.npcOption(mordJatizso, "Travel-Rellekka")
        tick(18)
        assertEquals(Tile(2644, 3710), player.tile)
    }

    @Test
    fun `Sail to Neitiznot and back`() {
        skipMissing()
        val player = createPlayer(Tile(2643, 3710))
        val maria = createNPC("maria_gunnars_rellekka", Tile(2644, 3711))

        player.npcOption(maria, "Travel-Neitiznot")
        tick(19)
        assertEquals(Tile(2310, 3782), player.tile)

        val mariaNeitiznot = createNPC("maria_gunnars", Tile(2311, 3781))
        player.npcOption(mariaNeitiznot, "Travel-Rellekka")
        tick(19)
        assertEquals(Tile(2644, 3710), player.tile)
    }

    @Test
    fun `Sail to Miscellania and back`() {
        skipMissing()
        val player = createPlayer(Tile(2628, 3692))
        val sailor = createNPC("sailor_rellekka", Tile(2629, 3693))

        player.npcOption(sailor, "Travel-Miscellania")
        tick(14)
        assertEquals(Tile(2581, 3845), player.tile)

        val sailorMisc = createNPC("sailor_miscellania", Tile(2581, 3847))
        player.npcOption(sailorMisc, "Travel-Rellekka")
        tick(12)
        assertEquals(Tile(2629, 3693), player.tile)
    }

    @Test
    fun `Can't sail to the isles without the Fremennik Trials`() {
        val player = createPlayer(Tile(2643, 3709))
        val mord = createNPC("mord_gunnars_rellekka", Tile(2644, 3709))

        player.npcOption(mord, "Travel-Jatizso")
        tick(3)

        assertEquals(Tile(2643, 3709), player.tile)
        assertTrue(player.containsMessage("You must have completed The Fremennik Trials quest."))
    }

    @Test
    fun `Outerlanders pay Jarvald to reach Waterbirth Island`() {
        val player = createPlayer(Tile(2620, 3683))
        player.inventory.add("coins", 1000)
        val jarvald = createNPC("jarvald_rellekka", Tile(2621, 3683))

        player.npcOption(jarvald, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick(13)

        assertEquals(Tile(2544, 3759), player.tile)
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Jarvald won't sail without the fare`() {
        val player = createPlayer(Tile(2620, 3683))
        val jarvald = createNPC("jarvald_rellekka", Tile(2621, 3683))

        player.npcOption(jarvald, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick(2)

        assertEquals(Tile(2620, 3683), player.tile)
        assertTrue(player.containsMessage("You cannot afford that."))
    }
}
