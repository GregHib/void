package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.player
import kotlin.test.assertEquals

internal class ChoiceTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        mockkStatic("world.gregs.voidps.world.interact.dialogue.type.PlayerDialogueKt")
        coEvery { context.player(any(), any()) } just Runs
    }

    @TestFactory
    fun `Send choice lines`() = arrayOf(
        """
            One
            Two
        """ to "dialogue_multi2",
        "One\nTwo\nThree" to "dialogue_multi3",
        "One\nTwo\nThree\nFour" to "dialogue_multi4",
        "One\nTwo\nThree\nFour\nFive" to "dialogue_multi5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                choice(text = text)
            }
            verify {
                player.open(expected)
                for ((index, line) in text.trimIndent().lines().withIndex()) {
                    interfaces.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @TestFactory
    fun `Send multi line choice lines`() = arrayOf(
        """
            One
            Two<br>Three
        """ to "dialogue_multi2_chat",
        "One\nTwo\nThree<br>Four" to "dialogue_multi3_chat",
        "One\nTwo<br>Five\nThree\nFour" to "dialogue_multi4_chat",
        "One\nTwo\nThree\nFour<br>Six\nFive" to "dialogue_multi5_chat"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                choice(text = text)
            }
            verify {
                player.open(expected)
                for ((index, line) in text.trimIndent().lines().withIndex()) {
                    interfaces.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @TestFactory
    fun `Send multi line title choice lines`() = arrayOf(
        """
            One
            Two
        """ to "dialogue_multi_var2",
        "One\nTwo\nThree" to "dialogue_multi_var3",
        "One\nTwo\nThree\nFour" to "dialogue_multi_var4",
        "One\nTwo\nThree\nFour\nFive" to "dialogue_multi_var5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                choice(text = text, title = "First<br>Second")
            }
            verify {
                player.open(expected)
                for ((index, line) in text.trimIndent().lines().withIndex()) {
                    interfaces.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @Test
    fun `Sending six or more lines throws exception`() {
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                choice(text = "\nOne\nTwo\nThree\nFour\nFive\nSix")
            }
        }
    }

    @Test
    fun `Sending less than two lines throws exception`() {
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                choice(text = "One line")
            }
        }
    }

    @Test
    fun `Send no title`() {
        dialogue {
            choice(text = "Yes\nNo", title = null)
        }
        verify {
            player.open("dialogue_multi2")
        }
        verify(exactly = 0) {
            interfaces.sendText("dialogue_multi2", "title", any())
        }
    }

    @Test
    fun `Send multiline title`() {
        dialogue {
            choice(text = "Yes\nNo", title = """
                A long title that exceeds
                maximum width but is split
            """)
        }
        verify {
            player.open("dialogue_multi_var2")
            interfaces.sendText("dialogue_multi_var2", "title", "A long title that exceeds<br>maximum width but is split")
            interfaces.sendVisibility("dialogue_multi_var2", "wide_swords", false)
        }
    }

    @Test
    fun `Send wide multiline title`() {
        dialogue {
            choice(text = "Yes\nNo", title = """
                A long title that exceeds maximum
                and is on two lines
            """)
        }
        verify {
            player.open("dialogue_multi_var2")
            interfaces.sendText("dialogue_multi_var2", "title", "A long title that exceeds maximum<br>and is on two lines")
            interfaces.sendVisibility("dialogue_multi_var2", "wide_swords", true)
        }
    }

    @Test
    fun `Choice not sent if interface not opened`() {
        every { player.open("dialogue_multi2") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                choice(text = "Yes\nNo")
            }
        }
        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_multi2", "line1", "Yes")
            interfaces.sendText("dialogue_multi2", "line2", "No")
        }
    }

    @Test
    fun `Send choice`() {
        var result = -1
        dialogue {
            result = choice(text = "Yes\nNo")
        }
        val suspend = player.dialogueSuspension as IntSuspension
        suspend.int = 1
        suspend.resume()
        assertEquals(1, result)
        coVerify {
            interfaces.sendText("dialogue_multi2", "line1", "Yes")
            interfaces.sendText("dialogue_multi2", "line2", "No")
        }
    }

    private suspend fun CharacterContext<Player>.choice(text: String, title: String? = null): Int {
        val lines = text.trimIndent().lines()
        return choice(lines, title)
    }
}
