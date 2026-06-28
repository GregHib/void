package content.social.report

import WorldTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PunishmentsTest : WorldTest() {

    @Test
    fun `Black marks accumulate up to the permanent punishment limit`() = runTest {
        val player = createPlayer(name = "offender")

        repeat(BLACK_MARK_LIMIT) {
            player.addBlackMark(Rule.MacroingOrUseOfBots)
        }

        assertEquals(BLACK_MARK_LIMIT, player.blackMarks)
    }

    @Test
    fun `Expired black marks degrade`() = runTest {
        val player = createPlayer(name = "offender")
        player["black_marks"] = listOf("7:1", "15:$PERMANENT")

        assertEquals(1, player.blackMarks)
        assertEquals(listOf("15:$PERMANENT"), player.activeBlackMarks())
    }

    @Test
    fun `Real world trading marks never expire`() = runTest {
        val player = createPlayer(name = "offender")

        player.addBlackMark(Rule.BreakingRealWorldLaws)

        assertTrue(player.activeBlackMarks().single().endsWith(":$PERMANENT"))
    }

    @Test
    fun `Mute adds a black mark`() = runTest {
        val player = createPlayer(name = "offender")

        player.mute()

        assertEquals(1, player.blackMarks)
    }

    @Test
    fun `Mute for a report records the rule broken`() = runTest {
        val player = createPlayer(name = "offender")

        player.mute(rule = Rule.Scamming)

        assertTrue(player.activeBlackMarks().single().startsWith("${Rule.Scamming.id}:"))
    }

    @Test
    fun `Ban adds a black mark`() = runTest {
        val player = createPlayer(name = "offender")

        player.ban()

        assertEquals(1, player.blackMarks)
    }

    @Test
    fun `Banned player flag`() = runTest {
        val player = createPlayer(name = "offender")
        assertFalse(player.isBanned)

        player.ban(48)
        assertTrue(player.isBanned)

        player.unban()
        assertFalse(player.isBanned)

        player.permBan()
        assertTrue(player.isBanned)
    }
}
