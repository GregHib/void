package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.test.mock.declareMock
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendSprite
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.suspend.dialogue.ContinueSuspension
import world.gregs.voidps.world.interact.dialogue.type.item
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
