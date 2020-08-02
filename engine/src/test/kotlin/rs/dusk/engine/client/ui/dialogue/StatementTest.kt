package rs.dusk.engine.client.ui.dialogue

import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.client.ui.open

internal class StatementTest : DialogueIOTest() {

    @TestFactory
    fun `Send statement lines`() = arrayOf(
        "One line" to "message1",
        """
            One
            Two
        """ to "message2",
        "One\nTwo\nThree" to "message3",
        "One\nTwo\nThree\nFour" to "message4",
        "One\nTwo\nThree\nFour\nFive" to "message5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            io.sendStatement(DialogueBuilder(player, text = text, clickToContinue = true))
            verify {
                player.open(expected)
                for((index, line) in text.trimIndent().lines().withIndex()) {
                    manager.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @TestFactory
    fun `Send click to continue statement lines`() = arrayOf(
        "One line" to "message_np1",
        """
            One
            Two
        """ to "message_np2",
        "One\nTwo\nThree" to "message_np3",
        "One\nTwo\nThree\nFour" to "message_np4",
        "One\nTwo\nThree\nFour\nFive" to "message_np5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            io.sendStatement(DialogueBuilder(player, text = text, clickToContinue = false))
            verify {
                player.open(expected)
                for((index, line) in text.trimIndent().lines().withIndex()) {
                    manager.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @Test
    fun `Sending six or more lines is ignored`() {
        io.sendStatement(DialogueBuilder(player, text = "\nOne\nTwo\nThree\nFour\nFive\nSix"))
        verify(exactly = 0) {
            player.open(any())
        }
    }

    @Test
    fun `Statement not sent if interface not opened`() {
        every { player.open("message1") } returns false
        io.sendStatement(DialogueBuilder(player, text = "text"))
        verify(exactly = 0) {
            manager.sendText("message1", "line1", "text")
        }
    }
}