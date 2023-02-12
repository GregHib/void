package world.gregs.voidps.world.interact.dialogue

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
import world.gregs.voidps.engine.client.ui.sendAnimation
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.data.definition.extra.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.extra.getComponentOrNull
import world.gregs.voidps.engine.suspend.ContinueSuspension
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.playerDialogueHead
import world.gregs.voidps.world.interact.dialogue.type.player
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
                player(text = text, clickToContinue = true, expression = "talk")
            }
            verify {
                player.open(expected)
                for ((index, line) in text.trimIndent().lines().withIndex()) {
                    interfaces.sendText(expected, "line${index + 1}", line)
                }
            }
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
                player(text = text, clickToContinue = false, expression = "talk")
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
                player(text = "\nOne\nTwo\nThree\nFour\nFive", expression = "talk")
            }
        }
        verify(exactly = 0) {
            player.open(any())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Send player chat head size and animation`(large: Boolean) {
        mockkStatic("world.gregs.voidps.network.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitionsKt")
        val client: Client = mockk(relaxed = true)
        player.client = client
        val definition: InterfaceDefinition = mockk(relaxed = true)
        every { definitions.get("dialogue_chat1") } returns definition
        every { definition.getComponentOrNull(any()) } returns InterfaceComponentDefinition(id = 123, extras = mapOf("parent" to 4))
        dialogue {
            player(text = "Text", largeHead = large, expression = "talk")
        }
        verify {
            client.playerDialogueHead(4, 123)
            interfaces.sendAnimation("dialogue_chat1", if (large) "head_large" else "head", 9803)
        }
    }

    @Test
    fun `Send custom player chat title`() {
        dialogue {
            player(text = "text", title = "Bob", expression = "talk")
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
            player(text = "text", largeHead = true, expression = "laugh")
            resumed = true
        }
        (player.suspension as ContinueSuspension).resume()
        coVerify {
            interfaces.sendText("dialogue_chat1", "title", "Jim")
            interfaces.sendAnimation("dialogue_chat1", "head_large", 9840)
        }
        assertTrue(resumed)
    }

    @Test
    fun `NPC chat not sent if interface not opened`() {
        every { player.open("dialogue_chat1") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                player(text = "text", expression = "talk")
            }
        }
        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_chat1", "line1", "text")
        }
    }
}
