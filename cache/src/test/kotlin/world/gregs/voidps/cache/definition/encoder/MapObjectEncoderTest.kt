package world.gregs.voidps.cache.definition.encoder

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject

internal class MapObjectEncoderTest {

    @Test
    fun `Write two objects with same id`() {
        // Given
        val map = MapDefinition(123)
        map.objects.add(MapObject(12345, 63, 63, 3, 4, 1))
        map.objects.add(MapObject(12345, 0, 0, 0, 12, 2))
        // When
        val encoder = MapObjectEncoder()
        val writer = BufferWriter()
        with(encoder) {
            writer.encode(map)
        }
        // Then
        assertArrayEquals(byteArrayOf(-80, 58, 1, 50, -64, 0, 17, 0, 0), writer.toArray())
    }

    @Test
    fun `Write two objects with same tile`() {
        // Given
        val map = MapDefinition(123)
        map.objects.add(MapObject(12345, 54, 45, 0, 12, 2))
        map.objects.add(MapObject(42000, 54, 45, 0, 0, 0))
        // When
        val encoder = MapObjectEncoder()
        val writer = BufferWriter()
        with(encoder) {
            writer.encode(map)
        }
        // Then
        assertArrayEquals(byteArrayOf(-80, 58, -115, -82, 50, 0, -13, -41, -115, -82, 0, 0, 0), writer.toArray())
    }

    @Test
    fun `Write id over 65k`() {
        // Given
        val map = MapDefinition(123)
        map.objects.add(MapObject(75000, 54, 45, 0, 12, 2))
        // When
        val encoder = MapObjectEncoder()
        val writer = BufferWriter()
        with(encoder) {
            writer.encode(map)
        }
        // Then
        assertArrayEquals(byteArrayOf(-1, -1, -1, -1, -92, -5, -115, -82, 50, 0, 0), writer.toArray())
    }
}
