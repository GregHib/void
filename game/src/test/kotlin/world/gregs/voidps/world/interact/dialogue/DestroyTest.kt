package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.world.interact.dialogue.type.destroy
import world.gregs.voidps.world.script.KoinMock

internal class DestroyTest : KoinMock() {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var player: Player
    lateinit var context: DialogueContext

    override val modules = listOf(cacheDefinitionModule)

    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.client.ui.InterfacesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        manager = spyk(Dialogues())
        context = mockk(relaxed = true)
        every { context.player } returns player
        every { player.open(any()) } returns true
        every { player.interfaces } returns interfaces
        declareMock<ItemDefinitions> {
            every { this@declareMock.get("1234") } returns ItemDefinition(id = 1234, name = "magic")
        }
    }

    @Test
    fun `Send item destroy`() {
        manager.start(context) {
            destroy("""
                question
                lines
            """, "1234")
        }
        runBlocking(Contexts.Game) {
            verify {
                player.open("confirm_destroy")
                interfaces.sendText("confirm_destroy", "line1", "question<br>lines")
                interfaces.sendText("confirm_destroy", "item_name", "magic")
                interfaces.sendItem("confirm_destroy", "item_slot", 1234, 1)
            }
        }
    }

    @Test
    fun `Destroy not sent if interface not opened`() {
        coEvery { context.await<Boolean>(any()) } returns false
        every { player.open("confirm_destroy") } returns false
        manager.start(context) {
            destroy("question", "1234")
        }

        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Boolean>("destroy")
                interfaces.sendText("confirm_destroy", "line1", "question")
            }
        }
    }

    @Test
    fun `Destroy returns boolean`() {
        coEvery { context.await<Boolean>(any()) } returns true
        manager.start(context) {
            assertTrue(destroy("question", "1234"))
        }
    }
}