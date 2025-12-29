package world.gregs.voidps.cache.config.encoder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.Parameters

class StructEncoderTest {

    private val parameters = object : Parameters {
        override val parameters: Map<Int, String>
            get() = mapOf(0 to "test")
    }

    private val encoder = StructEncoder(mapOf("test" to 0))
    private val decoder = StructDecoder(parameters)

    @Test
    fun `Complete encoding and decoding a string`() {
        val writer: Writer = ArrayWriter(20)
        val definition = StructDefinition(extras = mapOf("test" to "string"))
        with(encoder) {
            writer.encode(definition)
        }

        val actual = StructDefinition()
        val reader = ArrayReader(writer.toArray())
        decoder.readLoop(actual, reader)

        assertEquals(definition, actual)
    }

    @Test
    fun `Complete encoding and decoding an integer`() {
        val writer: Writer = ArrayWriter(20)
        val definition = StructDefinition(extras = mapOf("test" to 1234))
        with(encoder) {
            writer.encode(definition)
        }

        val actual = StructDefinition()
        val reader = ArrayReader(writer.toArray())
        decoder.readLoop(actual, reader)

        assertEquals(definition, actual)
    }

    @Test
    fun `Can't encode a custom parameter`() {
        val writer: Writer = ArrayWriter(20)
        val definition = StructDefinition(extras = mapOf("custom" to 1234))
        with(encoder) {
            writer.encode(definition)
        }

        val actual = StructDefinition()
        val reader = ArrayReader(writer.toArray())
        decoder.readLoop(actual, reader)

        assertEquals(StructDefinition.EMPTY, actual)
    }

    @Test
    fun `Can't encode a custom value type`() {
        val writer: Writer = ArrayWriter(20)
        val definition = StructDefinition(extras = mapOf("test" to listOf("")))
        with(encoder) {
            writer.encode(definition)
        }

        val actual = StructDefinition()
        val reader = ArrayReader(writer.toArray())
        decoder.readLoop(actual, reader)

        assertEquals(StructDefinition.EMPTY, actual)
    }
}
