package content.entity.player.dialogue

import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.test.mock.declareMock
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.suspend.ContinueSuspension
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
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
            item("item_name", 650, """
                An item
                description
            """, 10)
            resumed = true
        }
        (player.dialogueSuspension as ContinueSuspension).resume(Unit)
        verify {
            player.open("dialogue_obj_box")
            player.sendScript("dialogue_item_zoom", 9009, 650)
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
            items("item_name", "item2_name", "Item descriptions")
            resumed = true
        }
        (player.dialogueSuspension as ContinueSuspension).resume(Unit)
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
                item("9009", 650, "text", 10)
            }
        }
        coVerify(exactly = 0) {
            player.sendScript("dialogue_item_zoom", 650, 9009)
        }
    }
}
