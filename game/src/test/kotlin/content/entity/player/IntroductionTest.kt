package content.entity.player

import WorldTest
import dialogueContinue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.nameHistory
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.Suspension
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class IntroductionTest : WorldTest() {

    @Test
    fun `Choose a display name rejects invalid and taken names`() {
        createPlayer(name = "Bob")
        val player = welcome("newbie@example.com")

        player.enterName("bad_name!")
        assertTrue(player.suspension is Suspension.Continue)
        player.dialogueContinue()
        player.enterName("bob")
        assertTrue(player.suspension is Suspension.Continue)
        player.dialogueContinue()
        player.enterName("Newbie")

        assertNull(player.suspension)
        assertEquals("Newbie", player.name)
        val definitions: AccountDefinitions = get()
        assertNotNull(definitions.get("Newbie"))
        assertNull(definitions.get("newbie@example.com"))
        assertEquals("Newbie", definitions.getByAccount("newbie@example.com")?.displayName)
        assertEquals(listOf("newbie@example.com"), player.nameHistory)
        assertFalse(player["choose_name", false])
    }

    @Test
    fun `Keeping the current name clears the flag without renaming`() {
        val player = welcome("Keeper")

        player.enterName("Keeper")

        assertNull(player.suspension)
        assertEquals("Keeper", player.name)
        assertTrue(player.nameHistory.isEmpty())
        assertFalse(player["choose_name", false])
    }

    /**
     * Logs in a registered account which still has to choose its display name, without the character creation screen
     */
    private fun welcome(name: String): Player {
        settings["world.start.creation"] = "false"
        return createPlayer(name = name) {
            it.clear("creation")
            it["choose_name"] = true
        }
    }

    private fun Player.enterName(name: String) {
        val suspension = suspension as Suspension.NameEntry
        suspension.resume(name)
    }
}
