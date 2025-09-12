package content.entity.player.dialogue.type

import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Talk
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.suspend.ContinueSuspension
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.npcDialogueHead
import kotlin.test.assertTrue

internal class NPCChatTest : DialogueTest() {

    lateinit var npc: NPC

    @BeforeEach
    override fun setup() {
        super.setup()
        npc = NPC(id = "jim", index = -1, def = NPCDefinition(id = 123, stringId = "jim", name = "Jim"))
        player.talkWith(npc)
        declareMock<AnimationDefinitions> {
            every { this@declareMock.get(any<String>()) } returns AnimationDefinition()
            every { this@declareMock.get("expression_talk").id } returns 9803
            every { this@declareMock.getOrNull("expression_talk1")?.id } returns 9803
            every { this@declareMock.getOrNull("expression_talk2")?.id } returns 9803
            every { this@declareMock.getOrNull("expression_talk3")?.id } returns 9803
            every { this@declareMock.getOrNull("expression_talk4")?.id } returns 9803
            every { this@declareMock.get("expression_laugh").id } returns 9840
            every { this@declareMock.getOrNull("expression_laugh1")?.id } returns 9840
            every { this@declareMock.getOrNull("expression_laugh2")?.id } returns 9840
            every { this@declareMock.getOrNull("expression_laugh3")?.id } returns 9840
            every { this@declareMock.getOrNull("expression_laugh4")?.id } returns 9840
        }
        declareMock<NPCDefinitions> {
            every { this@declareMock.get(any<String>()) } returns NPCDefinition()
            every { this@declareMock.get("jim") } returns NPCDefinition(id = 123, name = "Jim")
        }
    }

    @TestFactory
    fun `Send lines npc chat`() = arrayOf(
        "One line" to "dialogue_npc_chat1",
        """
            One
            Two
        """ to "dialogue_npc_chat2",
        "One\nTwo\nThree" to "dialogue_npc_chat3",
        "One\nTwo\nThree\nFour" to "dialogue_npc_chat4",
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                npc<Talk>(text = text, clickToContinue = true)
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
    fun `Long line wraps npc chat`() {
        val text = "This is one long dialogue text line which should be wrapped into two lines."
        dialogue {
            npc<Talk>(text = text, clickToContinue = true)
        }
        verify {
            player.open("dialogue_npc_chat2")
            interfaces.sendText("dialogue_npc_chat2", "line1", "This is one long dialogue text line which should be")
            interfaces.sendText("dialogue_npc_chat2", "line2", "wrapped into two lines.")
        }
    }

    @TestFactory
    fun `Send click to continue npc chat`() = arrayOf(
        "One line" to "dialogue_npc_chat_np1",
        """
            One
            Two
        """ to "dialogue_npc_chat_np2",
        "One\nTwo\nThree" to "dialogue_npc_chat_np3",
        "One\nTwo\nThree\nFour" to "dialogue_npc_chat_np4",
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                npc<Talk>(text = text, clickToContinue = false)
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
    fun `Sending five or more lines to chat splits them up`() {
        dialogue {
            npc<Talk>(text = "\nOne\nTwo\nThree\nFour\nFive")
        }
        (player.dialogueSuspension as ContinueSuspension).resume(Unit)
        verify(exactly = 2) {
            player.open(any())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Send npc chat head size and animation`(large: Boolean) {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
        val client: Client = mockk(relaxed = true)
        player.client = client
        npc = NPC(id = "john")
        dialogue {
            npc<Talk>(text = "Text", largeHead = large)
        }
        verify {
            client.npcDialogueHead(InterfaceDefinition.pack(4, 321), 123)
            interfaces.sendAnimation("dialogue_npc_chat1", if (large) "head_large" else "head", 9803)
        }
    }

    @Test
    fun `Send custom npc chat title`() {
        dialogue {
            npc<Talk>(text = "text", title = "Bob")
        }
        verify {
            interfaces.sendText("dialogue_npc_chat1", "title", "Bob")
        }
    }

    @Test
    fun `Send npc chat`() {
        var resumed = false
        dialogue {
            npc<Laugh>(text = "text", largeHead = true)
            resumed = true
        }
        (player.dialogueSuspension as ContinueSuspension).resume(Unit)
        coVerify {
            interfaces.sendText("dialogue_npc_chat1", "title", "Jim")
            interfaces.sendAnimation("dialogue_npc_chat1", "head_large", 9840)
        }
        assertTrue(resumed)
    }

    @Test
    fun `NPC chat not sent if interface not opened`() {
        every { player.open("dialogue_npc_chat1") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                npc<Talk>(text = "text")
            }
        }
        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_npc_chat1", "line1", "text")
        }
    }

    @Test
    fun `Send different npc chat`() {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.engine.data.definition.InterfaceDefinitions")
        val client: Client = mockk(relaxed = true)
        player.client = client
        npc = NPC("bill")
        dialogue {
            npc<Talk>(npcId = "jim", title = "Bill", text = "text")
        }
        coVerify {
            interfaces.sendText("dialogue_npc_chat1", "title", "Bill")
            client.npcDialogueHead(InterfaceDefinition.pack(4, 321), 123)
            interfaces.sendText("dialogue_npc_chat1", "line1", "text")
        }
    }
}
