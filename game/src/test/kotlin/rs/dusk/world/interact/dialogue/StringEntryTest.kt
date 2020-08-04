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
import rs.dusk.world.interact.dialogue.type.stringEntry

internal class StringEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        manager.start(context) {
            stringEntry("text")
        }
        runBlocking(Contexts.Game) {
            assertEquals("string", manager.currentType())
            verify {
                player.send(ScriptMessage(109, "text"))
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