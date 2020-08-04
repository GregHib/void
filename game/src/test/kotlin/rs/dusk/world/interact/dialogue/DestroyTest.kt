package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.world.script.KoinMock

internal class DestroyTest : KoinMock() {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var player: Player

    override val modules = listOf(cacheDefinitionModule)

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        manager = spyk(Dialogues(player))
        every { player.open(any()) } returns true
        every { player.interfaces } returns interfaces
        declareMock<ItemDecoder> {
            every { getSafe(1234) } returns ItemDefinition(name = "magic")
        }
    }

    @Test
    fun `Send item destroy`() = runBlocking {
        manager.start {
            destroy("question", 1234)
        }
        withContext(Contexts.Game) {
            assertEquals("destroy", manager.currentType())
            verify {
                player.open("confirm_destroy")
                interfaces.sendText("confirm_destroy", "line1", "question")
                interfaces.sendText("confirm_destroy", "item_name", "magic")
                interfaces.sendItem("confirm_destroy", "item_slot", 1234, 1)
            }
        }
    }

    @Test
    fun `Destroy not sent if interface not opened`() = runBlocking {
        coEvery { manager.await<Boolean>(any()) } returns false
        every { player.open("confirm_destroy") } returns false
        manager.start {
            destroy("question", 1234)
        }

        withContext(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Boolean>("destroy")
                interfaces.sendText("confirm_destroy", "line1", "question")
            }
        }
    }

    @Test
    fun `Destroy returns boolean`() = runBlocking {
        coEvery { manager.await<Boolean>(any()) } returns true
        manager.start {
            assertTrue(destroy("question", 1234))
        }
    }
}