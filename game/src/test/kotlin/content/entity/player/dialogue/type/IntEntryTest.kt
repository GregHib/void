package content.entity.player.dialogue.type

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.suspend.Suspension
import kotlin.test.assertTrue

internal class IntEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        dialogue {
            intEntry("text")
        }
        assertTrue(player.suspension is Suspension.IntEntry)
        verify {
            player.sendScript("int_entry", "text")
        }
    }

    @Test
    fun `Int entry returns int`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        var result = -1
        dialogue {
            result = intEntry("text")
        }
        val suspend = player.suspension as Suspension.IntEntry
        suspend.resume(123)
        assertEquals(123, result)
    }
}
