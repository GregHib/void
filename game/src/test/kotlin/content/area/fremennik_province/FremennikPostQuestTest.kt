package content.area.fremennik_province

import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import itemOption
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import stringEntry
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class FremennikPostQuestTest : WorldTest() {

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
    fun `Bird's-Eye Jack opens the bank from talking`() {
        skipMissing(true)
        val player = createPlayer(Tile(2099, 3920))
        val jack = createNPC("birds_eye_jack_2", Tile(2099, 3921))

        assertEquals("birds_eye_jack_2_2", jack.def(player).stringId)
        player.npcOption(jack, "Talk-to")
        tick(2)
        player.dialogueOption(1) // Using the bank
        player.dialogueOption(1) // I'd like to access my bank account please.
        tick()

        assertTrue(player.hasOpen("bank"))
    }

    @Test
    fun `Bird's-Eye Jack hears about Cyrisus`() {
        skipMissing(true)
        val player = createPlayer(Tile(2099, 3920))
        val jack = createNPC("birds_eye_jack_2", Tile(2099, 3921))

        player.npcOption(jack, "Talk-to")
        tick(2)
        player.dialogueOption(2) // Cyrisus
        tick()

        assertEquals("dialogue_chat2", player.dialogue)
    }

    @Test
    fun `Erjolf talks about his trophy`() {
        skipMissing(true)
        val player = createPlayer(Tile(2695, 3793))
        val erjolf = createNPC("erjolf_base", Tile(2695, 3794))

        assertEquals("erjolf_2", erjolf.def(player).stringId)
        player.npcOption(erjolf, "Talk-to")
        tick(2)

        assertNotNull(player.dialogue)
    }

    @Test
    fun `Natural historian gives a lamp for finding the Muspah statue once`() {
        skipMissing(true)
        val player = createPlayer(Tile(2735, 3823, 1))
        val historian = createNPC("natural_historian_rellekka", Tile(2735, 3824, 1))

        assertEquals("natural_historian_rellekka_2", historian.def(player).stringId)
        player.npcOption(historian, "Talk-to")
        tick(2)
        player.dialogueContinue(7)

        assertEquals(1, player.inventory.count("antique_lamp_the_tale_of_the_muspah"))
        assertTrue(player["muspah_statue_reported", false])

        player.dialogueContinue()
        player.npcOption(historian, "Talk-to")
        tick(2)
        player.dialogueContinue(6)

        assertEquals(1, player.inventory.count("antique_lamp_the_tale_of_the_muspah"))
    }

    @Test
    fun `Muspah lamp gives 200 experience`() {
        val player = createPlayer(Tile(2735, 3823))
        player.experience.set(Skill.Agility, 1200.0)
        player.inventory.add("antique_lamp_the_tale_of_the_muspah")

        player.itemOption("Rub", "antique_lamp_the_tale_of_the_muspah")
        tick()
        player.stringEntry("agility")
        tick()

        assertEquals(1400.0, player.experience.get(Skill.Agility))
        assertEquals(0, player.inventory.count("antique_lamp_the_tale_of_the_muspah"))
    }

    @Test
    fun `Advisor Ghrim hands out his book`() {
        skipMissing(true)
        val player = createPlayer(Tile(2499, 3856, 1))
        val ghrim = createNPC("advisor_ghrim", Tile(2499, 3857, 1))

        assertEquals("advisor_ghrim_2", ghrim.def(player).stringId)
        player.npcOption(ghrim, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption(2) // How do I manage the kingdom?
        player.skipDialogues()

        assertEquals(1, player.inventory.count("ghrims_book"))
    }

    @Test
    fun `Jim explains the hunter area then takes kgp agents`() {
        skipMissing(true)
        val player = createPlayer(Tile(2670, 4003, 1))
        val jim = createNPC("jim_iceberg", Tile(2670, 4004, 1))

        assertEquals("jim_iceberg_3", jim.def(player).stringId)
        player.npcOption(jim, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(2) // Ignorance is bliss.
        player.dialogueContinue(2)

        assertTrue(player["jim_hunting_ground_explained", false])

        player.inventory.add("kgp_agent", 3)
        player.npcOption(jim, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption(1) // Yes.
        player.dialogueContinue(2)

        assertEquals(0, player.inventory.count("kgp_agent"))
        assertEquals(6.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Tickle Jim`() {
        skipMissing(true)
        val player = createPlayer(Tile(2670, 4003, 1))
        val jim = createNPC("jim_iceberg", Tile(2670, 4004, 1))

        player.npcOption(jim, "Tickle")
        tick(2)

        assertTrue(player.containsMessage("You tickle the polar bear."))
    }
}
