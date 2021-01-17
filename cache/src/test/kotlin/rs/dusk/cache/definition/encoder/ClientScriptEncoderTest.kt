package rs.dusk.cache.definition.encoder

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.buffer.write.BufferWriter
import rs.dusk.cache.Cache
import rs.dusk.cache.Indices.CLIENT_SCRIPTS
import rs.dusk.cache.definition.data.ClientScriptDefinition
import rs.dusk.cache.definition.decoder.ClientScriptDecoder

internal class ClientScriptEncoderTest {

    @Test
    fun `Encode full test`() {
        val definition = ClientScriptDefinition(
            id = 0,
            intArgumentCount = 2,
            stringVariableCount = 1,
            longVariableCount = 3,
            intVariableCount = 2,
            stringArgumentCount = 1,
            longArgumentCount = 3,
            aHashTableArray9503 = arrayOf(listOf(1 to 2), listOf(2 to 3, 3 to 4)),
            name = "client-script",
            instructions = intArrayOf(3, 54, 54, 54, 0, 21),
            stringOperands = arrayOf("one", null, null, null, null, null),
            longOperands = longArrayOf(0, 1, 2, 3, 0, 0),
            intOperands = intArrayOf(0, 0, 0, 0, 300, 2)
        )
        val encoder = ClientScriptEncoder()

        val writer = BufferWriter()
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.buffer.array().copyOf(writer.position())

        val cache: Cache = mockk(relaxed = true)
        every { cache.getFile(CLIENT_SCRIPTS, any(), any<Int>()) } returns data
        val decoder = ClientScriptDecoder(cache)
        val decoded = decoder.get(0)
        assertEquals(definition, decoded)
    }
}