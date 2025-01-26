package content.entity.player.dialogue.type

import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.suspend.ContinueSuspension
import kotlin.test.assertTrue

internal class StatementTest : DialogueTest() {

    @TestFactory
    fun `Send statement lines`() = arrayOf(
        "One line" to "dialogue_message1",
        """
            One
            Two
        """ to "dialogue_message2",
        "One\nTwo\nThree" to "dialogue_message3",
        "One\nTwo\nThree\nFour" to "dialogue_message4",
        "One\nTwo\nThree\nFour\nFive" to "dialogue_message5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                statement(text = text, clickToContinue = true)
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
    fun `Long line wraps statement`() {
        val text = "This is one very long statement dialogue text paragraph which should be wrapped into at least three lines but ideally even more than three, maybe four."
        dialogue {
            statement(text = text, clickToContinue = true)
        }
        verify {
            player.open("dialogue_message3")
            interfaces.sendText("dialogue_message3", "line1", "This is one very long statement dialogue text paragraph which should")
            interfaces.sendText("dialogue_message3", "line2", "be wrapped into at least three lines but ideally even more than three,")
            interfaces.sendText("dialogue_message3", "line3", "maybe four.")
        }
    }

    @TestFactory
    fun `Send click to continue statement lines`() = arrayOf(
        "One line" to "dialogue_message_np1",
        """
            One
            Two
        """ to "dialogue_message_np2",
        "One\nTwo\nThree" to "dialogue_message_np3",
        "One\nTwo\nThree\nFour" to "dialogue_message_np4",
        "One\nTwo\nThree\nFour\nFive" to "dialogue_message_np5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                statement(text = text, clickToContinue = false)
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
                statement(text = "\nOne\nTwo\nThree\nFour\nFive\nSix")
            }
        }
        verify(exactly = 0) {
            player.open(any())
        }
    }

    @Test
    fun `Send statement`() {
        var resumed = false
        dialogue {
            statement("text")
            resumed = true
        }
        (player.dialogueSuspension as ContinueSuspension).resume(Unit)
        coVerify {
            player.open("dialogue_message1")
            interfaces.sendText("dialogue_message1", "line1", "text")
        }
        assertTrue(resumed)
    }

    @Test
    fun `Statement not sent if interface not opened`() {
        every { player.open("dialogue_message1") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                statement("text")
            }
        }
        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_message1", "line1", "text")
        }
    }
}
