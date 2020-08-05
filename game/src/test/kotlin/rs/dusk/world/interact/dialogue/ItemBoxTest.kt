package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.open
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage
import rs.dusk.world.interact.dialogue.type.itemBox

internal class ItemBoxTest : DialogueTest() {

    @Test
    fun `Send item box`() {
        manager.start(context) {
            itemBox("""
                An item
                description
            """, 9009, 650, 10)
        }
        runBlocking(Contexts.Game) {
            assertEquals("item", manager.currentType())
            verify {
                player.open("obj_box")
                player.send(ScriptMessage(3449, 9009, 650))
                interfaces.sendSprite("obj_box", "sprite", 10)
                interfaces.sendText("obj_box", "line1", "An item\ndescription")
            }
        }
    }

    @Test
    fun `Item box not sent if interface not opened`() {
        coEvery { context.await<Unit>(any()) } just Runs
        every { player.open("obj_box") } returns false
        manager.start(context) {
            itemBox("text", 9009, 650, 10)
        }

        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Unit>("item")
                player.send(ScriptMessage(3449, 9009, 650))
            }
        }
    }
}