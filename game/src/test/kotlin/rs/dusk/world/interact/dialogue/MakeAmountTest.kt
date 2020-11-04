package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.ui.InterfaceOptions
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.getVar
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.world.interact.dialogue.type.makeAmount
import rs.dusk.world.script.KoinMock

internal class MakeAmountTest : KoinMock() {

    lateinit var interfaces: Interfaces
    lateinit var interfaceOptions: InterfaceOptions
    lateinit var manager: Dialogues
    lateinit var player: Player
    lateinit var context: DialogueContext

    override val modules = listOf(cacheDefinitionModule)

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        mockkStatic("rs.dusk.engine.client.variable.VariablesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        interfaceOptions = mockk(relaxed = true)
        manager = spyk(Dialogues())
        context = mockk(relaxed = true)
        every { context.player } returns player
        coEvery { context.await<Int>(any()) } returns 0
        every { player.open(any()) } returns true
        every { player.setVar(any(), any<Int>()) } just Runs
        every { player.getVar(any(), any<Int>()) } returns 0
        every { player.interfaces } returns interfaces
        every { player.interfaceOptions } returns interfaceOptions
        declareMock<ItemDefinitions> {
            every { get(1) } returns ItemDefinition(name = "Jimmy")
            every { get(2) } returns ItemDefinition(name = "Jerome")
            every { get(3) } returns ItemDefinition(name = "Jorge")
        }
    }

    @Test
    fun `Send make amount dialogue`() {
        manager.start(context) {
            makeAmount(listOf(1, 2, 3), "ants", 25)
        }
        runBlocking(Contexts.Game) {
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
    fun `Persistent amount exceeding maximum will be capped`() {
        every { player.getVar("skill_creation_amount", any<Int>()) } returns 30
        manager.start(context) {
            makeAmount(listOf(1, 2, 3), "ants", 25)
        }
        runBlocking(Contexts.Game) {
            verify {
                player.setVar("skill_creation_maximum", 25)
                player.setVar("skill_creation_amount", 25)
            }
        }
    }

    @Test
    fun `Make amount not sent if interface not opened`() {
        every { player.open("skill_creation") } returns false
        manager.start(context) {
            makeAmount(listOf(1, 2, 3), "ants", 25)
        }
        runBlocking(Contexts.Game) {
            verify(exactly = 0) {
                player.setVar("skill_creation_type", "ants")
            }
        }
    }

    @Test
    fun `Make amount not sent if sub interface not opened`() {
        coEvery { context.await<Pair<Int, Int>>(any()) } returns (-1 to 0)
        every { player.open("skill_creation_amount") } returns false
        manager.start(context) {
            makeAmount(listOf(1, 2, 3), "ants", 25)
        }
        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Pair<Int, Int>>(any())
                player.setVar("skill_creation_type", "ants")
            }
        }
    }

    @Test
    fun `Make amount send text`() {
        manager.start(context) {
            makeAmount(listOf(1, 2, 3), "ants", 25, text = "Just a test")
        }
        runBlocking(Contexts.Game) {
            verify {
                interfaces.sendText("skill_creation_amount", "line1", "Just a test")
                interfaceOptions.unlockAll("skill_creation_amount", "all")
                interfaces.sendVisibility("skill_creation", "all", true)
                interfaces.sendVisibility("skill_creation", "custom", false)
            }
        }
    }

    @Test
    fun `Hide 'all' button`() {
        manager.start(context) {
            makeAmount(listOf(1, 2, 3), "ants", 25, text = "test", allowAll = false)
        }
        runBlocking(Contexts.Game) {
            verify(exactly = 0) {
                interfaceOptions.unlockAll("skill_creation_amount", "all")
            }
            verify {
                interfaces.sendText("skill_creation_amount", "line1", "test")
                interfaces.sendVisibility("skill_creation", "all", false)
            }
        }
    }
}