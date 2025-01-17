package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension
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
        assertTrue(player.dialogueSuspension is IntSuspension)
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
        val suspend = player.dialogueSuspension as IntSuspension
        suspend.resume(123)
        assertEquals(123, result)
    }

}
