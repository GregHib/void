package world.gregs.voidps.cache.definition.encoder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import java.nio.ByteBuffer

internal class EnumEncoderTest {

    @Test
    fun `Encode full test`() {
        val definition = EnumDefinition(
            id = 10,
            keyType = 'b',
            valueType = 'c',
            defaultString = "default",
            defaultInt = 123,
            length = 2,
            map = hashMapOf(1 to "string", 2 to "12345"),
        )
        val encoder = EnumEncoder()

        val writer = ArrayWriter(1024)
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.array()

        val decoder = EnumDecoder()
        val decodedDefinition = EnumDefinition(id = definition.id)
        val reader = ArrayReader(ByteBuffer.wrap(data))
        decoder.readLoop(decodedDefinition, reader)

        assertEquals(definition, decodedDefinition)
    }

    @Disabled
    @Test
    fun `Encode everything`() {
        val cache: Cache = CacheDelegate("../data/cache/")
        val decoder = EnumDecoder()
        val full = decoder.load(cache)
        val encoder = EnumEncoder()
        val writer = ArrayWriter(20_000)

        for (definition in full) {
            with(encoder) {
                writer.clear()
                writer.encode(definition)
            }
            val data = writer.array()

            val decodedDefinition = EnumDefinition(id = definition.id)
            val reader = ArrayReader(ByteBuffer.wrap(data))
            decoder.readLoop(decodedDefinition, reader)

            assertEquals(definition.keyType, decodedDefinition.keyType)
            assertEquals(definition.valueType, decodedDefinition.valueType)
            assertEquals(definition.defaultString, decodedDefinition.defaultString)
            assertEquals(definition.defaultInt, decodedDefinition.defaultInt)
            // Length can differ as enums can include values which override itself
            assertEquals(definition.map?.toSortedMap(), decodedDefinition.map?.toSortedMap())
        }
    }
}
