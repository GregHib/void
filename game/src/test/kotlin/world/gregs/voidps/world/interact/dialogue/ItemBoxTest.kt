package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendSprite
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.world.interact.dialogue.type.item

internal class ItemBoxTest : DialogueTest() {

    @Test
    fun `Send item box`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        declareMock<ItemDefinitions> {
            every { this@declareMock.get("item_name").id } returns 9009
        }
        every { player.sendScript(any(), *anyVararg()) } just Runs
        manager.start(context) {
            item("""
                An item
                description
            """, "item_name", 650, 10)
        }
        runBlocking(Contexts.Game) {
            assertEquals("item", manager.currentType())
            verify {
                player.open("dialogue_obj_box")
                player.sendScript(3449, 9009, 650)
                interfaces.sendSprite("dialogue_obj_box", "sprite", 10)
                interfaces.sendText("dialogue_obj_box", "line1", "An item<br>description")
            }
        }
    }

    @Test
    fun `Item box not sent if interface not opened`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        coEvery { context.await<Unit>(any()) } just Runs
        every { player.open("dialogue_obj_box") } returns false
        manager.start(context) {
            item("text", "9009", 650, 10)
        }

        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Unit>("item")
                player.sendScript(3449, 650, 9009)
            }
        }
    }
}