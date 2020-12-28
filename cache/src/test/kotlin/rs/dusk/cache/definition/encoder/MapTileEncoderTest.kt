package rs.dusk.cache.definition.encoder

import org.junit.jupiter.api.Test
import rs.dusk.buffer.write.BufferWriter
import rs.dusk.cache.definition.data.MapDefinition
import rs.dusk.cache.definition.data.MapTile

internal class MapTileEncoderTest {

    @Test
    fun `Write settings`() {
        // Given
        val def = MapDefinition(123)
        var count = 1
        for (plane in 0 until 2) {
            for (x in 0 until 2) {
                for (y in 0 until 2) {
                    def.setTile(x, y, plane, MapTile(0, 0, 0, count++, 0))
                }
            }
        }
        val encoder = MapTileEncoder()
        val writer = BufferWriter()
        // When
        with(encoder) {
            writer.encode(def)
        }
        // Then
//        assertArrayEquals(byteArrayOf(50, 0, 51, 0, 52, 0, 53, 0, 54, 0, 55, 0, 56, 0, 57, 0), writer.toArray())
    }
}