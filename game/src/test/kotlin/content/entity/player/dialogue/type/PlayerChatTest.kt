package content.entity.player.dialogue.type

import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Talk
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.suspend.ContinueSuspension
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.playerDialogueHead
import kotlin.test.assertTrue

internal class PlayerChatTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        player.accountName = "John"
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
    }

    @TestFactory
    fun `Send lines player chat`() = arrayOf(
        "One line" to "dialogue_chat1",
        """
            One
            Two
        """ to "dialogue_chat2",
        "One\nTwo\nThree" to "dialogue_chat3",
        "One\nTwo\nThree\nFour" to "dialogue_chat4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                player<Talk>(text = text, clickToContinue = true)
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
    fun `Long line wraps player chat`() {
        val text = "This is one long dialogue text line which should be wrapped into two lines."
        dialogue {
            player<Talk>(text = text, clickToContinue = true)
        }
        verify {
            player.open("dialogue_chat2")
            interfaces.sendText("dialogue_chat2", "line1", "This is one long dialogue text line which should be")
            interfaces.sendText("dialogue_chat2", "line2", "wrapped into two lines.")
        }
    }

    @TestFactory
    fun `Send click to continue player chat`() = arrayOf(
        "One line" to "dialogue_chat_np1",
        """
            One
            Two
        """ to "dialogue_chat_np2",
        "One\nTwo\nThree" to "dialogue_chat_np3",
        "One\nTwo\nThree\nFour" to "dialogue_chat_np4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                player<Talk>(text = text, clickToContinue = false)
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
    fun `Sending five or more lines to chat throws exception`() {
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                player<Talk>(text = "\nOne\nTwo\nThree\nFour\nFive")
            }
        }
        verify(exactly = 0) {
            player.open(any())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Send player chat head size and animation`(large: Boolean) {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.engine.data.definition.InterfaceDefinitionsKt")
        val client: Client = mockk(relaxed = true)
        player.client = client
        every { interfaceDefinitions.getComponent("dialogue_chat1", any<String>()) } returns InterfaceComponentDefinition(id = InterfaceDefinition.pack(4, 123))
        dialogue {
            player<Talk>(text = "Text", largeHead = large)
        }
        verify {
            client.playerDialogueHead(InterfaceDefinition.pack(4, 123))
            interfaces.sendAnimation("dialogue_chat1", if (large) "head_large" else "head", 9803)
        }
    }

    @Test
    fun `Send custom player chat title`() {
        dialogue {
            player<Talk>(text = "text", title = "Bob")
        }
        runBlocking(Contexts.Game) {
            verify {
                interfaces.sendText("dialogue_chat1", "title", "Bob")
            }
        }
    }

    @Test
    fun `Send player chat`() {
        player.accountName = "Jim"
        var resumed = false
        dialogue {
            player<Laugh>(text = "text", largeHead = true)
            resumed = true
        }
        (player.dialogueSuspension as ContinueSuspension).resume(Unit)
        coVerify {
            interfaces.sendText("dialogue_chat1", "title", "Jim")
            interfaces.sendAnimation("dialogue_chat1", "head_large", 9840)
        }
        assertTrue(resumed)
    }

    @Test
    fun `Player chat not sent if interface not opened`() {
        every { player.open("dialogue_chat1") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                player<Talk>(text = "text")
            }
        }
        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_chat1", "line1", "text")
        }
    }
}
