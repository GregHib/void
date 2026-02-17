package content.entity.player.dialogue.type

import KoinMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ClientScriptDefinitions
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

abstract class DialogueTest : KoinMock() {

    lateinit var interfaces: Interfaces
    lateinit var player: Player
    lateinit var continuation: Continuation<Any>
    lateinit var fontDefinitions: FontDefinitions
    lateinit var clientScriptDefinitions: ClientScriptDefinitions

    fun dialogueBlocking(block: suspend Player.() -> Unit) {
        runTest {
            block.invoke(player)
        }
    }

    fun dialogue(block: suspend Player.() -> Unit) {
        GlobalScope.launch(Dispatchers.Unconfined) {
            block.invoke(player)
        }
    }

    @BeforeEach
    open fun setup() {
        mockkStatic("world.gregs.voidps.engine.client.ui.InterfacesKt")
        player = spyk(Player())
        interfaces = mockk(relaxed = true)
        player.interfaces = interfaces
        InterfaceDefinitions.set(
            arrayOf(InterfaceDefinition(components = mutableMapOf(0 to InterfaceComponentDefinition(id = InterfaceDefinition.pack(4, 321))))),
            mapOf("" to 0, "dialogue_level_up" to 0, "dialogue_npc_chat1" to 0, "dialogue_chat1" to 0),
            mapOf("" to 0, "dialogue_level_up" to 0, "dialogue_npc_chat1:head_large" to 0, "dialogue_npc_chat1:head" to 0, "dialogue_chat1" to 0),
        )
        fontDefinitions = declareMock()
        clientScriptDefinitions = declareMock()
        continuation = object : Continuation<Any> {
            override val context: CoroutineContext
                get() = UnconfinedTestDispatcher()

            override fun resumeWith(result: Result<Any>) {
            }
        }
        every { clientScriptDefinitions.get("string_entry") } returns ClientScriptDefinition(id = 109)
        every { player.open(any()) } returns true

        val glyphWidths = DialogueTest::class.java.getResourceAsStream("glyph-widths-497.dat")!!.readAllBytes()
        every { fontDefinitions.get(any<String>()) } returns FontDefinition(id = 497, verticalSpacing = 15, topPadding = 15, bottomPadding = 15, glyphWidths = glyphWidths)
    }
}
