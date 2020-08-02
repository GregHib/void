package rs.dusk.engine.client.ui.dialogue

import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.update.visual.player.name

internal class PlayerChatTest : DialogueIOTest() {

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
            io.sendChat(DialogueBuilder(player, text = text, clickToContinue = true))
            verify {
                player.open(expected)
                for((index, line) in text.trimIndent().lines().withIndex()) {
                    manager.sendText(expected, "line${index + 1}", line)
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
            io.sendChat(DialogueBuilder(player, text = text, clickToContinue = false))
            verify {
                player.open(expected)
                for((index, line) in text.trimIndent().lines().withIndex()) {
                    manager.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @Test
    fun `Sending five or more lines to chat is ignored`() {
        io.sendChat(DialogueBuilder(player, text = "\nOne\nTwo\nThree\nFour\nFive"))
        verify(exactly = 0) {
            player.open(any())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Send player chat head size and animation`(large: Boolean) {
        io.sendChat(DialogueBuilder(player, text = "Text", large = large, expression = Expression.Talking))
        verify {
            manager.sendPlayerHead("chat1", if(large) "head_large" else "head")
            manager.sendAnimation("chat1", if(large) "head_large" else "head", 9803)
        }
    }

    @Test
    fun `Send custom player chat title`() {
        io.sendChat(DialogueBuilder(player, text = "text", title = "Bob"))
        verify {
            manager.sendText("chat1", "title", "Bob")
        }
    }

    @Test
    fun `Send player chat title`() {
        every { player.name } returns "Jim"
        io.sendChat(DialogueBuilder(player, text = "text"))
        verify {
            manager.sendText("chat1", "title", "Jim")
        }
    }

    @Test
    fun `NPC chat not sent if interface not opened`() {
        every { player.open("chat1") } returns false
        io.sendChat(DialogueBuilder(player, text = "text"))
        verify(exactly = 0) {
            manager.sendText("chat1", "line1", "text")
        }
    }
}