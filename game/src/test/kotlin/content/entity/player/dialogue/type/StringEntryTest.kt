package content.entity.player.dialogue.type

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.suspend.StringSuspension

internal class StringEntryTest : DialogueTest() {

    @Test
    fun `Send int entry`() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
        dialogue {
            stringEntry("text")
        }
        verify {
            player.sendScript("string_entry", "text")
        }
    }

    @Test
    fun `String entry returns string`() {
        var result = ""
        dialogue {
            result = stringEntry("text")
        }
        val suspend = player.dialogueSuspension as StringSuspension
        suspend.resume("a string")
        assertEquals("a string", result)
    }

}
