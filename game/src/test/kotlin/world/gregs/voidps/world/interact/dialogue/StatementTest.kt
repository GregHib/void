package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.world.interact.dialogue.type.statement

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
            manager.start(context) {
                statement(text = text, clickToContinue = true)
            }
            runBlocking(Contexts.Game) {
                verify {
                    player.open(expected)
                    for ((index, line) in text.trimIndent().lines().withIndex()) {
                        interfaces.sendText(expected, "line${index + 1}", line)
                    }
                }
            }
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
            manager.start(context) {
                statement(text = text, clickToContinue = false)
            }
            runBlocking(Contexts.Game) {
                verify {
                    player.open(expected)
                    for ((index, line) in text.trimIndent().lines().withIndex()) {
                        interfaces.sendText(expected, "line${index + 1}", line)
                    }
                }
            }
        }
    }

    @Test
    fun `Sending six or more lines is ignored`() {
        manager.start(context) {
            statement(text = "\nOne\nTwo\nThree\nFour\nFive\nSix")
        }
        runBlocking(Contexts.Game) {
            verify(exactly = 0) {
                player.open(any())
            }
        }
    }

    @Test
    fun `Send statement`() {
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            statement("text")
        }
        runBlocking(Contexts.Game) {
            coVerify {
                player.open("dialogue_message1")
                interfaces.sendText("dialogue_message1", "line1", "text")
                context.await<Unit>("statement")
            }
        }
    }

    @Test
    fun `Statement not sent if interface not opened`() {
        coEvery { context.await<Unit>(any()) } just Runs
        every { player.open("dialogue_message1") } returns false
        manager.start(context) {
            statement("text")
        }

        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Unit>("statement")
                interfaces.sendText("dialogue_message1", "line1", "text")
            }
        }
    }
}