package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.ui.open
import rs.dusk.network.rs.codec.game.encode.sendScript
import rs.dusk.world.interact.dialogue.type.item

internal class ItemBoxTest : DialogueTest() {

    @Test
    fun `Send item box`() {
        mockkStatic("rs.dusk.network.rs.codec.game.encode.ScriptMessageEncoderKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        manager.start(context) {
            item("""
                An item
                description
            """, 9009, 650, 10)
        }
        runBlocking(Contexts.Game) {
            assertEquals("item", manager.currentType())
            verify {
                player.open("obj_box")
                player.sendScript(3449, 9009, 650)
                interfaces.sendSprite("obj_box", "sprite", 10)
                interfaces.sendText("obj_box", "line1", "An item<br>description")
            }
        }
    }

    @Test
    fun `Item box not sent if interface not opened`() {
        mockkStatic("rs.dusk.network.rs.codec.game.encode.ScriptMessageEncoderKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        coEvery { context.await<Unit>(any()) } just Runs
        every { player.open("obj_box") } returns false
        manager.start(context) {
            item("text", 9009, 650, 10)
        }

        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Unit>("item")
                player.sendScript(3449, 650, 9009)
            }
        }
    }
}