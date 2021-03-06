package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.ui.dialogue.Expression
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.world.interact.dialogue.type.player

internal class PlayerChatTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.AppearanceKt")
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
            manager.start(context) {
                player(text = text, clickToContinue = true)
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
            manager.start(context) {
                player(text = text, clickToContinue = false)
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
        manager.start(context) {
            player(text = "\nOne\nTwo\nThree\nFour\nFive")
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
        manager.start(context) {
            player(text = "Text", largeHead = large, expression = Expression.Talking)
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
        manager.start(context) {
            player(text = "text", title = "Bob")
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
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            player(text = "text", largeHead = true, expression = Expression.Laugh)
        }
        runBlocking(Contexts.Game) {
            coVerify {
                context.await<Unit>("chat")
                interfaces.sendText("chat1", "title", "Jim")
                interfaces.sendAnimation("chat1", "head_large", 9840)
            }
        }
    }

    @Test
    fun `NPC chat not sent if interface not opened`() {
        every { player.open("chat1") } returns false
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            player(text = "text")
        }
        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Unit>("chat")
                interfaces.sendText("chat1", "line1", "text")
            }
        }
    }
}