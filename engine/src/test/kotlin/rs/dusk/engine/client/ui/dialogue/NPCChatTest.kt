package rs.dusk.engine.client.ui.dialogue

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.npc.NPC

internal class NPCChatTest : DialogueIOTest() {

    lateinit var npc: NPC

    @BeforeEach
    override fun setup() {
        super.setup()
        npc = mockk()
        every { npc.id } returns 123
        every { npc.def.name } returns ""
    }

    @TestFactory
    fun `Send lines npc chat`() = arrayOf(
        "One line" to "npc_chat1",
        """
            One
            Two
        """ to "npc_chat2",
        "One\nTwo\nThree" to "npc_chat3",
        "One\nTwo\nThree\nFour" to "npc_chat4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            io.sendChat(DialogueBuilder(npc, text = text, clickToContinue = true))
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
        "One line" to "npc_chat_np1",
        """
            One
            Two
        """ to "npc_chat_np2",
        "One\nTwo\nThree" to "npc_chat_np3",
        "One\nTwo\nThree\nFour" to "npc_chat_np4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            io.sendChat(DialogueBuilder(npc, text = text, clickToContinue = false))
            verify {
                player.open(expected)
                for((index, line) in text.trimIndent().lines().withIndex()) {
                    manager.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Send npc chat head size, animation and id`(large: Boolean) {
        io.sendChat(DialogueBuilder(npc, text = "Text", large = large, expression = Expression.Talking))
        verify {
            manager.sendNPCHead("npc_chat1", if(large) "head_large" else "head", 123)
            manager.sendAnimation("npc_chat1", if(large) "head_large" else "head", 9803)
        }
    }

    @Test
    fun `Send custom npc chat title`() {
        io.sendChat(DialogueBuilder(npc, text = "text", title = "Bob"))
        verify {
            manager.sendText("npc_chat1", "title", "Bob")
        }
    }

    @Test
    fun `Send npc chat title`() {
        every { npc.def.name } returns "Jim"
        io.sendChat(DialogueBuilder(npc, text = "text"))
        verify {
            manager.sendText("npc_chat1", "title", "Jim")
        }
    }

    @Test
    fun `NPC chat not sent if interface not opened`() {
        every { player.open("npc_chat1") } returns false
        io.sendChat(DialogueBuilder(npc, text = "text"))
        verify(exactly = 0) {
            manager.sendText("npc_chat1", "line1", "text")
        }
    }
}