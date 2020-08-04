package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.dialogue.Expression
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.player.name

internal class PlayerChatTest {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var player: Player

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        mockkStatic("rs.dusk.engine.entity.character.update.visual.player.AppearanceKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        manager = spyk(Dialogues(player))
        every { player.open(any()) } returns true
        every { player.interfaces } returns interfaces
        every { player.name } returns "John"
    }


    @TestFactory
    fun `Send lines player chat`() = arrayOf(
        "One line" to "chat1",
        """
            One
            Two
        """ to "chat2",
        "One\nTwo\nThree" to "chat3",
        "One\nTwo\nThree\nFour" to "chat4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            manager.start {
                say(text = text, clickToContinue = true)
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
    fun `Send click to continue player chat`() = arrayOf(
        "One line" to "chat_np1",
        """
            One
            Two
        """ to "chat_np2",
        "One\nTwo\nThree" to "chat_np3",
        "One\nTwo\nThree\nFour" to "chat_np4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            manager.start {
                say(text = text, clickToContinue = false)
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
    fun `Sending five or more lines to chat is ignored`() {
        manager.start {
            say(text = "\nOne\nTwo\nThree\nFour\nFive")
        }
        runBlocking(Contexts.Game) {
            verify(exactly = 0) {
                player.open(any())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Send player chat head size and animation`(large: Boolean) {
        manager.start {
            say(text = "Text", largeHead = large, expression = Expression.Talking)
        }
        runBlocking(Contexts.Game) {
            verify {
                interfaces.sendPlayerHead("chat1", if (large) "head_large" else "head")
                interfaces.sendAnimation("chat1", if (large) "head_large" else "head", 9803)
            }
        }
    }

    @Test
    fun `Send custom player chat title`() {
        manager.start {
            say(text = "text", title = "Bob")
        }
        runBlocking(Contexts.Game) {
            verify {
                interfaces.sendText("chat1", "title", "Bob")
            }
        }
    }

    @Test
    fun `Send player chat`() {
        every { player.name } returns "Jim"
        coEvery { manager.await<Unit>(any()) } just Runs
        manager.start {
            say(text = "text", largeHead = true, expression = Expression.Laugh)
        }
        runBlocking(Contexts.Game) {
            coVerify {
                manager.await<Unit>("player")
                interfaces.sendText("chat1", "title", "Jim")
                interfaces.sendAnimation("chat1", "head_large", 9840)
            }
        }
    }

    @Test
    fun `NPC chat not sent if interface not opened`() {
        every { player.open("chat1") } returns false
        coEvery { manager.await<Unit>(any()) } just Runs
        manager.start {
            say(text = "text")
        }
        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Unit>("player")
                interfaces.sendText("chat1", "line1", "text")
            }
        }
    }
}