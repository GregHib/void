package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.sendVariable
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class MakeAmountTest : DialogueTest() {

    lateinit var interfaceOptions: InterfaceOptions

    @BeforeEach
    override fun setup() {
        super.setup()
        mockkStatic("world.gregs.voidps.engine.client.variable.VariablesKt")
        interfaceOptions = mockk(relaxed = true)
        every { player.sendVariable(any()) } just Runs
        every { player[any()] = any<Int>() } just Runs
        every { player[any(), any<Int>()] } returns 0
        player.interfaceOptions = interfaceOptions
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
        every { player.get<Int>("skill_creation_amount") } returns 3
        var result: Pair<String, Int>? = null
        dialogue {
            result = makeAmount(listOf("1", "2", "3"), "ants", 25)
        }
        val suspend = player.dialogueSuspension as IntSuspension
        suspend.int = 1
        suspend.resume()

        assertNotNull(result)
        assertEquals("2", result!!.first)
        assertEquals(3, result!!.second)
        verify {
            player.open("dialogue_skill_creation")
            player.open("skill_creation_amount")
            interfaces.sendVisibility("dialogue_skill_creation", "custom", false)
            player["skill_creation_type"] = "ants"
            player["skill_creation_item_0"] = 1
            player["skill_creation_name_0"] = "Jimmy"
            player["skill_creation_item_1"] = 2
            player["skill_creation_name_1"] = "Jerome"
            player["skill_creation_item_2"] = 3
            player["skill_creation_name_2"] = "Jorge"

            player["skill_creation_maximum"] = 25
        }
    }

    @Test
    fun `Persistent amount exceeding maximum will be capped`() {
        every { player["skill_creation_amount", any<Int>()] } returns 30
        dialogue {
            makeAmount(listOf("1", "2", "3"), "ants", 25)
        }
        verify {
            player["skill_creation_maximum"] = 25
            player["skill_creation_amount"] = 25
        }
    }

    @Test
    fun `Make amount not sent if interface not opened`() {
        every { player.open("dialogue_skill_creation") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                makeAmount(listOf("1", "2", "3"), "ants", 25)
            }
        }
        verify(exactly = 0) {
            player["skill_creation_type"] = "ants"
        }
    }

    @Test
    fun `Make amount not sent if sub interface not opened`() {
        every { player.open("skill_creation_amount") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                makeAmount(listOf("1", "2", "3"), "ants", 25)
            }
        }
        coVerify(exactly = 0) {
            player["skill_creation_type"] = "ants"
        }
    }

    @Test
    fun `Make amount send text`() {
        dialogue {
            makeAmount(listOf("1", "2", "3"), "ants", 25, text = "Just a test")
        }
        verify {
            interfaces.sendText("skill_creation_amount", "line1", "Just a test")
            interfaceOptions.unlockAll("skill_creation_amount", "all")
            interfaces.sendVisibility("dialogue_skill_creation", "all", true)
            interfaces.sendVisibility("dialogue_skill_creation", "custom", false)
        }
    }

    @Test
    fun `Hide 'all' button`() {
        dialogue {
            makeAmount(listOf("1", "2", "3"), "ants", 25, text = "test", allowAll = false)
        }
        verify(exactly = 0) {
            interfaceOptions.unlockAll("skill_creation_amount", "all")
        }
        verify {
            interfaces.sendText("skill_creation_amount", "line1", "test")
            interfaces.sendVisibility("dialogue_skill_creation", "all", false)
        }
    }
}
