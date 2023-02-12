package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.world.interact.dialogue.type.intEntry
import kotlin.test.assertTrue

internal class IntEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        dialogue {
            intEntry("text")
        }
        assertTrue(player.suspension is IntSuspension)
        verify {
            player.sendScript(108, "text")
        }
    }

    @Test
    fun `Int entry returns int`() {
        var result = -1
        dialogue {
            result = intEntry("text")
        }
        val suspend = player.suspension as IntSuspension
        suspend.int = 123
        suspend.resume()
        assertEquals(123, result)
    }

}
