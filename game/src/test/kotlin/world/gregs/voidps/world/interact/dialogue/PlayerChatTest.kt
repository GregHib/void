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
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.playerDialogueHead
import world.gregs.voidps.world.interact.dialogue.type.player

internal class PlayerChatTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        every { player.name } returns "John"
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
            manager.start(context) {
                player(text = text, clickToContinue = true, expression = "talk")
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
        "One line" to "dialogue_chat_np1",
        """
            One
            Two
        """ to "dialogue_chat_np2",
        "One\nTwo\nThree" to "dialogue_chat_np3",
        "One\nTwo\nThree\nFour" to "dialogue_chat_np4"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            manager.start(context) {
                player(text = text, clickToContinue = false, expression = "talk")
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
            player(text = "\nOne\nTwo\nThree\nFour\nFive", expression = "talk")
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
        every { definitions.get("dialogue_chat1") } returns definition
        every { definition.getComponentOrNull(any()) } returns InterfaceComponentDefinition(id = 123, extras = mapOf("parent" to 4))
        manager.start(context) {
            player(text = "Text", largeHead = large, expression = "talk")
        }
        runBlocking(Contexts.Game) {
            verify {
                client.playerDialogueHead(4, 123)
                interfaces.sendAnimation("dialogue_chat1", if (large) "head_large" else "head", 9803)
            }
        }
    }

    @Test
    fun `Send custom player chat title`() {
        manager.start(context) {
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
        every { player.name } returns "Jim"
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            player(text = "text", largeHead = true, expression = "laugh")
        }
        runBlocking(Contexts.Game) {
            coVerify {
                context.await<Unit>("chat")
                interfaces.sendText("dialogue_chat1", "title", "Jim")
                interfaces.sendAnimation("dialogue_chat1", "head_large", 9840)
            }
        }
    }

    @Test
    fun `NPC chat not sent if interface not opened`() {
        every { player.open("dialogue_chat1") } returns false
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            player(text = "text", expression = "talk")
        }
        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Unit>("chat")
                interfaces.sendText("dialogue_chat1", "line1", "text")
            }
        }
    }
}