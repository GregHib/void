package content.entity.player.dialogue.type

import content.entity.player.dialogue.Cackle
import content.entity.player.dialogue.Neutral
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.playerDialogueHead
import kotlin.test.assertTrue

internal class PlayerChatTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        player.accountName = "John"
        AnimationDefinitions.set(
            arrayOf(
                AnimationDefinition(9803, stringId = "expression_neutral"),
                AnimationDefinition(9840, stringId = "expression_cackle"),
                AnimationDefinition(9840, stringId = "expression_laugh"),
            ),
            mapOf(
                "expression_neutral" to 0,
                "expression_neutral1" to 0,
                "expression_neutral2" to 0,
                "expression_neutral3" to 0,
                "expression_neutral4" to 0,
                "expression_cackle" to 1,
                "expression_laugh1" to 2,
                "expression_laugh2" to 2,
                "expression_laugh3" to 2,
                "expression_laugh4" to 2,
            ),
        )
    }

    @TestFactory
    fun `Send lines player chat`() = arrayOf(
        "One line" to "dialogue_chat1",
        """
            One
            Two
        """ to "dialogue_chat2",
        "One\nTwo\nThree" to "dialogue_chat3",
        "One\nTwo\nThree\nFour" to "dialogue_chat4",
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                player<Neutral>(text = text, clickToContinue = true)
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
            player<Neutral>(text = text, clickToContinue = true)
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
        "One\nTwo\nThree\nFour" to "dialogue_chat_np4",
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            dialogue {
                player<Neutral>(text = text, clickToContinue = false)
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
    fun `Sending five or more lines to splits into multiple messages`() {
        dialogue {
            player<Neutral>(text = "\nOne\nTwo\nThree\nFour\nFive")
        }
        (player.suspension as Suspension.Continue).resume()
        verifyOrder {
            player.open("dialogue_chat4")
            interfaces.sendText("dialogue_chat4", "line1", "One")
            interfaces.sendText("dialogue_chat4", "line2", "Two")
            interfaces.sendText("dialogue_chat4", "line3", "Three")
            interfaces.sendText("dialogue_chat4", "line4", "Four")
            player.open("dialogue_chat1")
            interfaces.sendText("dialogue_chat1", "line1", "Five")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Send player chat head size and animation`(large: Boolean) {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.engine.data.definition.InterfaceDefinitions")
        val client: Client = mockk(relaxed = true)
        player.client = client
        InterfaceDefinitions.set(
            arrayOf(InterfaceDefinition(components = mutableMapOf(0 to InterfaceComponentDefinition(id = InterfaceDefinition.pack(4, 123))))),
            mapOf("dialogue_chat1" to 0),
            mapOf("dialogue_chat1:head_large" to 0, "dialogue_chat1:head" to 0),
        )
        dialogue {
            player<Neutral>(text = "Text", largeHead = large)
        }
        verify {
            client.playerDialogueHead(InterfaceDefinition.pack(4, 123))
            interfaces.sendAnimation("dialogue_chat1", if (large) "head_large" else "head", 9803)
        }
    }

    @Test
    fun `Send custom player chat title`() {
        dialogue {
            player<Neutral>(text = "text", title = "Bob")
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
            player<Cackle>(text = "text", largeHead = true)
            resumed = true
        }
        (player.suspension as Suspension.Continue).resume()
        coVerify {
            interfaces.sendText("dialogue_chat1", "title", "Jim")
            interfaces.sendAnimation("dialogue_chat1", "head_large", 9840)
        }
        assertTrue(resumed)
    }

    @Test
    fun `Player chat not sent if interface not opened`() {
        every { player.open("dialogue_chat1") } returns false
        dialogueBlocking {
            player<Neutral>(text = "text")
        }
        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_chat1", "line1", "text")
        }
    }
}
