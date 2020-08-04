package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
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
import rs.dusk.engine.client.variable.getVar
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.world.script.KoinMock

internal class MakeAmountTest : KoinMock() {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var player: Player

    override val modules = listOf(cacheDefinitionModule)

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        mockkStatic("rs.dusk.engine.client.variable.VariablesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        manager = spyk(Dialogues(player))
        every { player.open(any()) } returns true
        every { player.setVar(any(), any<Int>()) } just Runs
        every { player.getVar(any(), any<Int>()) } returns 0
        every { player.interfaces } returns interfaces
        declareMock<ItemDecoder> {
            every { getSafe(1) } returns ItemDefinition(name = "Jimmy")
            every { getSafe(2) } returns ItemDefinition(name = "Jerome")
            every { getSafe(3) } returns ItemDefinition(name = "Jorge")
        }
    }

    @Test
    fun `Send make amount dialogue`() = runBlocking {
        manager.start {
            makeAmount(listOf(1, 2, 3), "ants", 25)
        }
        withContext(Contexts.Game) {
            assertEquals("make", manager.currentType())
            verify {
                player.open("skill_creation")
                player.open("skill_creation_amount")
                interfaces.sendVisibility("skill_creation", "custom", false)
                player.setVar("skill_creation_type", "ants")
                player.setVar("skill_creation_item_0", 1)
                player.setVar("skill_creation_name_0", "Jimmy")
                player.setVar("skill_creation_item_1", 2)
                player.setVar("skill_creation_name_1", "Jerome")
                player.setVar("skill_creation_item_2", 3)
                player.setVar("skill_creation_name_2", "Jorge")

                player.setVar("skill_creation_maximum", 25)
            }
        }
    }

    @Test
    fun `Persistent amount exceeding maximum will be capped`() = runBlocking {
        every { player.getVar("skill_creation_amount", any<Int>()) } returns 30
        manager.start {
            makeAmount(listOf(1, 2, 3), "ants", 25)
        }
        withContext(Contexts.Game) {
            assertEquals("make", manager.currentType())
            verify {
                player.setVar("skill_creation_maximum", 25)
                player.setVar("skill_creation_amount", 25)
            }
        }
    }

    @Test
    fun `Make amount not sent if interface not opened`() = runBlocking {
        every { player.open("skill_creation") } returns false
        manager.start {
            makeAmount(listOf(1, 2, 3), "ants", 25)
        }
        withContext(Contexts.Game) {
            verify(exactly = 0) {
                player.setVar("skill_creation_type", "ants")
            }
        }
    }

    @Test
    fun `Make amount not sent if sub interface not opened`() = runBlocking {
        coEvery { manager.await<Pair<Int, Int>>(any()) } returns (-1 to 0)
        every { player.open("skill_creation_amount") } returns false
        manager.start {
            makeAmount(listOf(1, 2, 3), "ants", 25)
        }
        withContext(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Pair<Int, Int>>(any())
                player.setVar("skill_creation_type", "ants")
            }
        }
    }

    @Test
    fun `Make amount send text`() = runBlocking {
        manager.start {
            makeAmount(listOf(1, 2, 3), "ants", 25, text = "Just a test")
        }
        withContext(Contexts.Game) {
            assertEquals("make", manager.currentType())
            verify {
                interfaces.sendText("skill_creation_amount", "line1", "Just a test")
            }
        }
    }
}