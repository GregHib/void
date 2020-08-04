package rs.dusk.world.interact.dialogue

import io.mockk.coEvery
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.send
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage
import rs.dusk.world.interact.dialogue.type.intEntry

internal class IntEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        manager.start(context) {
            intEntry("text")
        }
        runBlocking(Contexts.Game) {
            assertEquals("int", manager.currentType())
            verify {
                player.send(ScriptMessage(108, "text"))
            }
        }
    }

    @Test
    fun `Int entry returns int`()  {
        coEvery { context.await<Int>("int") } returns 123
        manager.start(context) {
            assertEquals(123, intEntry("text"))
        }
    }

}