package rs.dusk.engine.map.region.obj

import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
internal class GameObjectMapWriterTest {

    val writer = GameObjectMapWriter()

    @Test
    fun `Write two objects with same id`() {
        // Given
        val map = sortedMapOf(
            12345 to listOf(
                GameObject(12345, Tile(0, 0), 12, 2),
                GameObject(12345, Tile(63, 63, 3), 4, 1)
            )
        )
        // When
        val result = writer.write(map)
        // Then
        assert(result.contentEquals(byteArrayOf(-80, 58, 1, 50, -64, 0, 17, 0, 0)))
    }

    @Test
    fun `Write two objects with same tile`() {
        // Given
        val tile = Tile(54, 45)
        val map = sortedMapOf(
            12345 to listOf(GameObject(12345, tile, 12, 2)),
            42000 to listOf(GameObject(42000, tile, 0, 0))
        )
        // When
        val result = writer.write(map)
        // Then
        assert(result.contentEquals(byteArrayOf(-80, 58, -115, -82, 50, 0, -13, -41, -115, -82, 0, 0, 0)))
    }

    @Test
    fun `Write id over 65k`() {
        // Given
        val tile = Tile(54, 45)
        val map = sortedMapOf(
            75000 to listOf(GameObject(75000, tile, 12, 2))
        )
        // When
        val result = writer.write(map)
        // Then
        assert(result.contentEquals(byteArrayOf(-1, -1, -1, -1, -92, -5, -115, -82, 50, 0, 0)))
    }
}