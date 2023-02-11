/*
package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.world.interact.dialogue.type.makeAmount

internal class MakeAmountTest : DialogueTest() {

    lateinit var interfaceOptions: InterfaceOptions

    @BeforeEach
    override fun setup() {
        super.setup()
        mockkStatic("world.gregs.voidps.engine.client.variable.VariablesKt")
        interfaceOptions = mockk(relaxed = true)
        every { context.player } returns player
        coEvery { context.await<Int>(any()) } returns 0
        every { player.setVar(any(), any<Int>()) } just Runs
        every { player.getVar(any(), any<Int>()) } returns 0
        every { player.interfaceOptions } returns interfaceOptions
        declareMock<ItemDefinitions> {
            every { this@declareMock.get("1").id } returns 1
            every { this@declareMock.get("2").id } returns 2
            every { this@declareMock.get("3").id } returns 3
            every { this@declareMock.get("") } returns ItemDefinition()
            every { this@declareMock.get("1") } returns ItemDefinition(id = 1, name = "Jimmy")
            every { this@declareMock.get("2") } returns ItemDefinition(id = 2, name = "Jerome")
            every { this@declareMock.get("3") } returns ItemDefinition(id = 3, name = "Jorge")
        }
    }

    @Test
    fun `Send make amount dialogue`() {
        manager.start(context) {
            makeAmount(listOf("1", "2", "3"), "ants", 25)
        }
        runBlocking(Contexts.Game) {
            verify {
                player.open("dialogue_skill_creation")
                player.open("skill_creation_amount")
                interfaces.sendVisibility("dialogue_skill_creation", "custom", false)
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
            makeAmount(listOf("1", "2", "3"), "ants", 25)
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
        every { player.open("dialogue_skill_creation") } returns false
        manager.start(context) {
            makeAmount(listOf("1", "2", "3"), "ants", 25)
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
            makeAmount(listOf("1", "2", "3"), "ants", 25)
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
            makeAmount(listOf("1", "2", "3"), "ants", 25, text = "Just a test")
        }
        runBlocking(Contexts.Game) {
            verify {
                interfaces.sendText("skill_creation_amount", "line1", "Just a test")
                interfaceOptions.unlockAll("skill_creation_amount", "all")
                interfaces.sendVisibility("dialogue_skill_creation", "all", true)
                interfaces.sendVisibility("dialogue_skill_creation", "custom", false)
            }
        }
    }

    @Test
    fun `Hide 'all' button`() {
        manager.start(context) {
            makeAmount(listOf("1", "2", "3"), "ants", 25, text = "test", allowAll = false)
        }
        runBlocking(Contexts.Game) {
            verify(exactly = 0) {
                interfaceOptions.unlockAll("skill_creation_amount", "all")
            }
            verify {
                interfaces.sendText("skill_creation_amount", "line1", "test")
                interfaces.sendVisibility("dialogue_skill_creation", "all", false)
            }
        }
    }
}*/
