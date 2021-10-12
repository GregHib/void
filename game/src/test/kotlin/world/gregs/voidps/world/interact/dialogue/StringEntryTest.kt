package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.world.interact.dialogue.type.intEntry
import world.gregs.voidps.world.interact.dialogue.type.stringEntry

internal class StringEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        mockkStatic("world.gregs.voidps.network.encode.ScriptEncoderKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        manager.start(context) {
            stringEntry("text")
        }
        runBlocking(Contexts.Game) {
            assertEquals("string", manager.currentType())
            verify {
                player.sendScript(109, "text")
            }
        }
    }

    @Test
    fun `String entry returns string`() {
        coEvery { context.await<String>("string") } returns "a string"
        manager.start(context) {
            assertEquals("a string", intEntry("text"))
        }
    }

}