package world.gregs.void.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.void.engine.action.Contexts
import world.gregs.void.network.codec.game.encode.sendScript
import world.gregs.void.world.interact.dialogue.type.intEntry
import world.gregs.void.world.interact.dialogue.type.stringEntry

internal class StringEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        mockkStatic("world.gregs.void.network.codec.game.encode.ScriptEncoderKt")
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