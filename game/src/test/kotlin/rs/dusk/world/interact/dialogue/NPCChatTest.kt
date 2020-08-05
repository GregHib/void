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
import rs.dusk.engine.client.ui.dialogue.Expression
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.world.interact.dialogue.type.npc

internal class NPCChatTest : DialogueTest() {

    lateinit var npc: NPC

    @BeforeEach
    override fun setup() {
        super.setup()
        npc = mockk(relaxed = true)
        every { player.name } returns ""
        every { npc.def.name } returns "John"
        every { context.npcId } returns 123
        every { context.npcName } returns "John"
    }

    @TestFactory
    fun `Send lines player chat`() = arrayOf(
        "One line" to "npc_chat1",
        """
            One
            Two
        """ to "npc_chat2",
        "One\nTwo\nThree" to "npc_chat3",
        "One\nTwo\nThree\nFour" to "npc_chat4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            manager.start(context) {
                npc(text = text, clickToContinue = true)
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
        "One line" to "npc_chat_np1",
        """
            One
            Two
        """ to "npc_chat_np2",
        "One\nTwo\nThree" to "npc_chat_np3",
        "One\nTwo\nThree\nFour" to "npc_chat_np4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            manager.start(context) {
                npc(text = text, clickToContinue = false)
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
            npc(text = "\nOne\nTwo\nThree\nFour\nFive")
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
        every { npc.id } returns 123
        manager.start(context) {
            npc(text = "Text", largeHead = large, expression = Expression.Talking)
        }
        runBlocking(Contexts.Game) {
            verify {
                interfaces.sendNPCHead("npc_chat1", if (large) "head_large" else "head", 123)
                interfaces.sendAnimation("npc_chat1", if (large) "head_large" else "head", 9803)
            }
        }
    }

    @Test
    fun `Send custom player chat title`() {
        manager.start(context) {
            npc(text = "text", title = "Bob")
        }
        runBlocking(Contexts.Game) {
            verify {
                interfaces.sendText("npc_chat1", "title", "Bob")
            }
        }
    }

    @Test
    fun `Send player chat`() {
        every { context.npcId } returns 123
        every { context.npcName } returns "Jim"
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            npc(text = "text", largeHead = true, expression = Expression.Laugh)
        }
        runBlocking(Contexts.Game) {
            coVerify {
                context.await<Unit>("chat")
                interfaces.sendText("npc_chat1", "title", "Jim")
                interfaces.sendAnimation("npc_chat1", "head_large", 9840)
            }
        }
    }

    @Test
    fun `NPC chat not sent if interface not opened`() {
        every { player.open("npc_chat1") } returns false
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            npc(text = "text")
        }
        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Unit>("chat")
                interfaces.sendText("npc_chat1", "line1", "text")
            }
        }
    }

    @Test
    fun `Send different npc chat`() {
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            npc(id = 123, npcName = "Bill", text = "text")
        }
        runBlocking(Contexts.Game) {
            coVerify {
                interfaces.sendText("npc_chat1", "title", "Bill")
                interfaces.sendNPCHead("npc_chat1", "head", 123)
                interfaces.sendText("npc_chat1", "line1", "text")
                context.await<Unit>("chat")
            }
        }
    }
}