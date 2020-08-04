package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.dialogue.DialogueIO
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player

internal class StatementTest {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var io: DialogueIO
    lateinit var player: Player

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        io = mockk(relaxed = true)
        manager = spyk(Dialogues(io, player))
        every { player.open(any()) } returns true
        every { player.interfaces } returns interfaces
    }


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
            manager.start {
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
            manager.start {
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
        manager.start {
            statement(text = "\nOne\nTwo\nThree\nFour\nFive\nSix")
        }
        runBlocking(Contexts.Game) {
            verify(exactly = 0) {
                player.open(any())
            }
        }
    }

    @Test
    fun `Send statement`() = runBlocking {
        manager.start {
            statement("text")
        }
        withContext(Contexts.Game) {
            assertEquals("statement", manager.currentType())
            verify {
                player.open("message1")
                interfaces.sendText("message1", "line1", "text")
            }
        }
    }

    @Test
    fun `Statement not sent if interface not opened`() = runBlocking {
        coEvery { manager.await<Unit>(any()) } just Runs
        every { player.open("message1") } returns false
        manager.start {
            statement("text")
        }

        withContext(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Unit>("statement")
                interfaces.sendText("message1", "line1", "text")
            }
        }
    }
}