package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.world.interact.dialogue.type.intEntry

internal class IntEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        mockkStatic("world.gregs.voidps.network.encode.ScriptEncoderKt")
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