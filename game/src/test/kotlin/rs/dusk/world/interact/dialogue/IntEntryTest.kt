package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.engine.action.Contexts
import rs.dusk.network.codec.game.encode.sendScript
import rs.dusk.world.interact.dialogue.type.intEntry

internal class IntEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        mockkStatic("rs.dusk.network.rs.codec.game.encode.ScriptMessageEncoderKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        manager.start(context) {
            intEntry("text")
        }
        runBlocking(Contexts.Game) {
            assertEquals("int", manager.currentType())
            verify {
                player.sendScript(108, "text")
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