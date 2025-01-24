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
import world.gregs.voidps.engine.data.definition.ItemDefinitions
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
            destroy("1234", """
                question
                lines
            """)
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
                destroy("1234", "question")
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
            destroyed = destroy("1234", "question")
        }
        val suspend = player.dialogueSuspension as StringSuspension
        suspend.resume("confirm")
        assertTrue(destroyed)
    }
}
