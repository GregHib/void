package world.gregs.voidps.world.interact.dialogue

import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendItem
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.world.interact.dialogue.type.destroy

internal class DestroyTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        declareMock<ItemDefinitions> {
            every { this@declareMock.get("1234") } returns ItemDefinition(id = 1234, name = "magic")
        }
    }

    @Test
    fun `Send item destroy`() {
        dialogue {
            destroy("""
                question
                lines
            """, "1234")
        }
        verify {
            player.open("dialogue_confirm_destroy")
            interfaces.sendText("dialogue_confirm_destroy", "line1", "question<br>lines")
            interfaces.sendText("dialogue_confirm_destroy", "item_name", "magic")
            interfaces.sendItem("dialogue_confirm_destroy", "item_slot", 1234, 1)
        }
    }

    @Test
    fun `Destroy not sent if interface not opened`() {
        every { player.open("dialogue_confirm_destroy") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                destroy("question", "1234")
            }
        }
        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_confirm_destroy", "line1", "question")
        }
    }

    @Test
    fun `Destroy returns boolean`() {
        every { player.open("dialogue_confirm_destroy") } returns true
        var destroyed = false
        dialogue {
            destroyed = destroy("question", "1234")
        }
        val suspend = player.suspension as StringSuspension
        suspend.string = "confirm"
        suspend.resume()
        assertTrue(destroyed)
    }
}
