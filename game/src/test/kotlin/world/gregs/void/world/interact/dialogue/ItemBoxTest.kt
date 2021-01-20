package world.gregs.void.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.void.engine.action.Contexts
import world.gregs.void.engine.client.ui.open
import world.gregs.void.network.codec.game.encode.sendScript
import world.gregs.void.world.interact.dialogue.type.item

internal class ItemBoxTest : DialogueTest() {

    @Test
    fun `Send item box`() {
        mockkStatic("world.gregs.void.network.codec.game.encode.ScriptEncoderKt")
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
        mockkStatic("world.gregs.void.network.codec.game.encode.ScriptEncoderKt")
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