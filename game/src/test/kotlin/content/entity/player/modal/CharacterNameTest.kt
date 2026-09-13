package content.entity.player.modal

import WorldTest
import interfaceOption
import org.junit.jupiter.api.Test
import stringEntry
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.Suspension
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class CharacterNameTest : WorldTest() {

    @Test
    fun `Confirming appearance opens the name panel for registered accounts`() {
        val player = registered("newbie@example.com")

        player.interfaceOption("character_creation", "confirm", optionIndex = 0)

        assertFalse(player.interfaces.contains(player.interfaces.gameFrame))
        assertTrue(player["character_name_suggestion_0", ""].isNotEmpty())
        assertEquals(0, player["character_name_page", -1])
    }

    @Test
    fun `Confirming appearance closes for username accounts`() {
        val player = createPlayer(name = "Bob")
        player.open("character_creation")

        player.interfaceOption("character_creation", "confirm", optionIndex = 0)

        assertTrue(player.interfaces.contains(player.interfaces.gameFrame))
    }

    @Test
    fun `Continue rejects taken and invalid names then accepts a free one`() {
        createPlayer(name = "Bob")
        val player = registered("newbie@example.com")
        player.interfaceOption("character_creation", "confirm", optionIndex = 0)

        player.interfaceOption("character_creation", "continue", "Continue")
        tick()
        assertTrue(player.suspension is Suspension.StringEntry)
        player.stringEntry("bob")
        assertFalse(player.interfaces.contains(player.interfaces.gameFrame))
        assertEquals(0, player["character_name_page", -1])
        assertEquals("bob", player["character_name_base", ""])
        assertTrue(player["character_name_suggestion_0", ""].isNotEmpty())

        player.interfaceOption("character_creation", "continue", "Continue")
        tick()
        player.stringEntry("bad!name")
        assertFalse(player.interfaces.contains(player.interfaces.gameFrame))

        player.interfaceOption("character_creation", "continue", "Continue")
        tick()
        player.stringEntry("Cool_Name")

        assertEquals("Cool Name", player.name)
        assertNotNull(get<AccountDefinitions>().get("Cool Name"))
        assertFalse(player.contains("choose_name"))
        assertTrue(player.interfaces.contains(player.interfaces.gameFrame))
    }

    @Test
    fun `Suggestions page and can be picked`() {
        val player = registered("newbie@example.com")
        player.interfaceOption("character_creation", "confirm", optionIndex = 0)
        val first = player["character_name_suggestion_0", ""]

        player.interfaceOption("character_creation", "more_suggestions", "More Suggestions")
        assertEquals(1, player["character_name_page", -1])
        val second = player["character_name_suggestion_0", ""]
        assertTrue(second != first)
        player.interfaceOption("character_creation", "previous_suggestions", "Previous Suggestions")
        assertEquals(0, player["character_name_page", -1])
        assertEquals(first, player["character_name_suggestion_0", ""])

        player.interfaceOption("character_creation", "suggestion_0", "Suggestion 1")
        tick()
        player.stringEntry(first)

        assertTrue(DisplayNames.valid(first))
        assertEquals(first, player.name)
        assertTrue(player.interfaces.contains(player.interfaces.gameFrame))
    }

    private fun registered(email: String): Player {
        val player = createPlayer(name = email) {
            it["choose_name"] = true
        }
        player.open("character_creation")
        return player
    }
}
