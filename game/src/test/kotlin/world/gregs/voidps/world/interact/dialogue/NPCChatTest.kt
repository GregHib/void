package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendAnimation
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.npcDialogueHead
import world.gregs.voidps.world.interact.dialogue.type.npc

internal class NPCChatTest : DialogueTest() {

    lateinit var npc: NPC

    @BeforeEach
    override fun setup() {
        super.setup()
        npc = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.AppearanceKt")
        every { player.name } returns ""
        every { npc.def.name } returns "John"
        every { context.npcId } returns 123
        every { context.npcName } returns "John"
        declareMock<AnimationDefinitions> {
            every { this@declareMock.get(any<String>()) } returns AnimationDefinition()
            every { this@declareMock.getId("expression_talk") } returns 9803
            every { this@declareMock.getId("expression_laugh") } returns 9840
        }
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
                npc(text = text, clickToContinue = true, expression = "talk")
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
                npc(text = text, clickToContinue = false, expression = "talk")
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
            npc(text = "\nOne\nTwo\nThree\nFour\nFive", expression = "talk")
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
        mockkStatic("world.gregs.voidps.network.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.engine.entity.definition.InterfaceDefinitionsKt")
        val client: Client = mockk(relaxed = true)
        every { player.client } returns client
        val definition: InterfaceDefinition = mockk(relaxed = true)
        every { definitions.get("npc_chat1") } returns definition
        every { definition.getComponentOrNull(any()) } returns InterfaceComponentDefinition(id = 321, extras = mapOf("parent" to 4))
        every { npc.intId } returns 123
        manager.start(context) {
            npc(text = "Text", largeHead = large, expression = "talk")
        }
        runBlocking(Contexts.Game) {
            verify {
                client.npcDialogueHead(4, 321, 123)
                interfaces.sendAnimation("npc_chat1", if (large) "head_large" else "head", 9803)
            }
        }
    }

    @Test
    fun `Send custom player chat title`() {
        manager.start(context) {
            npc(text = "text", title = "Bob", expression = "talk")
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
            npc(text = "text", largeHead = true, expression = "laugh")
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
            npc(text = "text", expression = "talk")
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
        mockkStatic("world.gregs.voidps.network.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.engine.entity.definition.InterfaceDefinitionsKt")
        val client: Client = mockk(relaxed = true)
        every { player.client } returns client
        val definition: InterfaceDefinition = mockk(relaxed = true)
        every { definitions.get("npc_chat1") } returns definition
        every { definition.getComponentOrNull(any()) } returns InterfaceComponentDefinition(id = 321, extras = mapOf("parent" to 4))
        every { npc.intId } returns 123
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            npc(id = 123, npcName = "Bill", text = "text", expression = "talk")
        }
        runBlocking(Contexts.Game) {
            coVerify {
                interfaces.sendText("npc_chat1", "title", "Bill")
                client.npcDialogueHead(4, 321, 123)
                interfaces.sendText("npc_chat1", "line1", "text")
                context.await<Unit>("chat")
            }
        }
    }
}