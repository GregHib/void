package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.test.mock.declareMock
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.suspend.dialogue.ContinueSuspension
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.items
import kotlin.test.assertTrue

internal class ItemBoxTest : DialogueTest() {

    @Test
    fun `Send item box`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        var resumed = false
        declareMock<ItemDefinitions> {
            every { this@declareMock.get("item_name").id } returns 9009
        }
        every { player.sendScript(any(), *anyVararg()) } just Runs
        dialogue {
            item("""
                An item
                description
            """, "item_name", 650, 10)
            resumed = true
        }
        (player.dialogueSuspension as ContinueSuspension).resume()
        verify {
            player.open("dialogue_obj_box")
            player.sendScript(3449, 9009, 650)
            interfaces.sendSprite("dialogue_obj_box", "sprite", 10)
            interfaces.sendText("dialogue_obj_box", "line1", "An item<br>description")
        }
        assertTrue(resumed)
    }

    @Test
    fun `Send double item box`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        var resumed = false
        declareMock<ItemDefinitions> {
            every { this@declareMock.get("item_name").id } returns 9009
            every { this@declareMock.get("item2_name").id } returns 9010
        }
        dialogue {
            items("Item descriptions", "item_name", "item2_name")
            resumed = true
        }
        (player.dialogueSuspension as ContinueSuspension).resume()
        verify {
            player.open("dialogue_double_obj_box")
            interfaces.sendItem("dialogue_double_obj_box", "model1", 9009)
            interfaces.sendItem("dialogue_double_obj_box", "model2", 9010)
            interfaces.sendText("dialogue_double_obj_box", "line1", "Item descriptions")
        }
        assertTrue(resumed)
    }

    @Test
    fun `Item box not sent if interface not opened`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        every { player.open("dialogue_obj_box") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                item("text", "9009", 650, 10)
            }
        }
        coVerify(exactly = 0) {
            player.sendScript(3449, 650, 9009)
        }
    }
}
