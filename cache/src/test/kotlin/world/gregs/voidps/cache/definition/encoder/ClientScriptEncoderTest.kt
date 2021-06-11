package world.gregs.voidps.cache.definition.encoder

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices.CLIENT_SCRIPTS
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder

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
            switchStatementIndices = arrayOf(listOf(1 to 2), listOf(2 to 3, 3 to 4)),
            name = "client-script",
            instructions = intArrayOf(3, 54, 54, 54, 0, 21),
            stringOperands = arrayOf("one", null, null, null, null, null),
            longOperands = longArrayOf(0, 1, 2, 3, 0, 0),
            intOperands = intArrayOf(0, 0, 0, 0, 300, 2)
        )
        val revision634 = false
        val encoder = ClientScriptEncoder(revision634)

        val writer = BufferWriter(capacity = 256)
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.array().copyOf(writer.position())

        val cache: Cache = mockk(relaxed = true)
        every { cache.getFile(CLIENT_SCRIPTS, any(), any<Int>()) } returns data
        val decoder = ClientScriptDecoder(cache, revision634)
        val decoded = decoder.get(0)
        assertEquals(definition, decoded)
    }

    @Test
    fun `Encode 634 test`() {
        val definition = ClientScriptDefinition(
            id = 0,
            intArgumentCount = 2,
            stringVariableCount = 1,
            intVariableCount = 2,
            stringArgumentCount = 1,
            switchStatementIndices = arrayOf(listOf(1 to 2), listOf(2 to 3, 3 to 4)),
            name = "client-script",
            instructions = intArrayOf(3, 0, 21),
            stringOperands = arrayOf("one", null, null),
            intOperands = intArrayOf(0, 300, 2)
        )
        val revision634 = true
        val encoder = ClientScriptEncoder(revision634)

        val writer = BufferWriter(capacity = 256)
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.array().copyOf(writer.position())

        val cache: Cache = mockk(relaxed = true)
        every { cache.getFile(CLIENT_SCRIPTS, any(), any<Int>()) } returns data
        val decoder = ClientScriptDecoder(cache, revision634)
        val decoded = decoder.get(0)
        assertEquals(definition, decoded)
    }
}