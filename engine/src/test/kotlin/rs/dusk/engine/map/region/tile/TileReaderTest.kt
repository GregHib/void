package rs.dusk.engine.map.region.tile

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
internal class TileReaderTest {

    val reader = TileReader()

    @Test
    fun `Read plane, x and y`() {
        // Given
        val data = ByteArray((4 * 64 * 64) + 4)
        data[0] = 50
        data[66] = 51
        data[2] = 52
        data[4099] = 53

        // When
        val result = reader.read(data)
        // Then
        assertEquals(1, result[0][0][0])
        assertEquals(2, result[0][1][0])
        assertEquals(3, result[0][0][1])
        assertEquals(4, result[1][0][0])
    }

}