package rs.dusk.engine.model.world.map.tile

import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
internal class TileWriterTest {

    val writer = TileWriter()

    @Test
    fun `Write plane, x and y`() {
        // Given
        val settings: TileSettings = Array(2) { Array(2) { ByteArray(2) } }
        var count = 1
        for (plane in 0 until 2) {
            for (x in 0 until 2) {
                for (y in 0 until 2) {
                    settings[plane][x][y] = count++.toByte()
                }
            }
        }
        // When
        val result = writer.write(settings)
        // Then
        assert(result.contentEquals(byteArrayOf(50, 0, 51, 0, 52, 0, 53, 0, 54, 0, 55, 0, 56, 0, 57, 0)))
    }
}