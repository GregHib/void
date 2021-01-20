package world.gregs.void.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.void.engine.action.Contexts
import world.gregs.void.network.codec.game.encode.sendScript
import world.gregs.void.world.interact.dialogue.type.intEntry

internal class IntEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        mockkStatic("world.gregs.void.network.codec.game.encode.ScriptEncoderKt")
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