package rs.dusk.engine.map.collision

import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
internal class CollisionsTest {

    lateinit var data: MutableMap<Int, Int>
    lateinit var collisions: Collisions

    @BeforeEach
    fun setup() {
        data = spyk(mutableMapOf())
        collisions = spyk(Collisions(data))
    }

    @Test
    fun `Append flag`() {
        // Given
        val flag = 0x8
        data[Tile.getId(1, 2, 3)] = 0x4
        // When
        collisions.add(1, 2, 3, flag)
        // Then
        verify {
            data[Tile.getId(1, 2, 3)] = 0xC
        }
    }

    @Test
    fun `Add flag`() {
        // Given
        val flag = 0x8
        // When
        collisions.add(1, 2, 3, flag)
        // Then
        verify {
            data[Tile.getId(1, 2, 3)] = flag
        }
    }

    @Test
    fun `Set flag`() {
        // Given
        data[Tile.getId(1, 2, 3)] = 0x4
        val flag = 0x8
        // When
        collisions[1, 2, 3] = flag
        // Then
        verify {
            data[Tile.getId(1, 2, 3)] = flag
        }
    }

    @Test
    fun `Remove flag`() {
        // Given
        data[Tile.getId(1, 2, 3)] = 0x4
        // When
        collisions.remove(1, 2, 3, 0x4)
        // Then
        verify {
            data[Tile.getId(1, 2, 3)] = 0
        }
    }

    @Test
    fun `Reduce flag`() {
        // Given
        data[Tile.getId(1, 2, 3)] = 0xC
        // When
        collisions.remove(1, 2, 3, 0x4)
        // Then
        verify {
            data[Tile.getId(1, 2, 3)] = 0x8
        }
    }

    @Test
    fun `Get empty flag`() {
        // When
        val result = collisions[1, 2, 3]
        // Then
        assertEquals(0, result)
    }

    @Test
    fun `Flag collides`() {
        // Given
        data[Tile.getId(1, 2, 3)] = 0x4
        // When
        val result = collisions.check(1, 2, 3, 0x4)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Flag doesn't collide`() {
        // Given
        data[Tile.getId(1, 2, 3)] = 0x4
        // When
        val result = collisions.check(1, 2, 3, 0x8)
        // Then
        assertFalse(result)
    }
}