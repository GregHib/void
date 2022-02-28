package world.gregs.voidps.engine.map.collision

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.chunk.Chunk

internal class CollisionsTest {

    lateinit var data: Array<IntArray?>
    lateinit var collisions: Collisions

    @BeforeEach
    fun setup() {
        data = arrayOfNulls(256 * 256 * 4)
        collisions = spyk(Collisions(data, 0))
    }

    @Test
    fun `Append flag`() {
        // Given
        val flag = 0x8
        set(1, 2, 3, 0x4)
        // When
        collisions.add(1, 2, 3, flag)
        // Then
        assertEquals(0xC, 1, 2, 3)
    }

    @Test
    fun `Add flag`() {
        // Given
        val flag = 0x8
        // When
        collisions.add(1, 2, 3, flag)
        // Then
        assertEquals(flag, 1, 2, 3)
    }

    @Test
    fun `Set flag`() {
        // Given
        set(1, 2, 3, 0x4)
        val flag = 0x8
        // When
        collisions[1, 2, 3] = flag
        // Then
        assertEquals(flag, 1, 2, 3)
    }

    @Test
    fun `Remove flag`() {
        // Given
        set(1, 2, 3, 0x4)
        // When
        collisions.remove(1, 2, 3, 0x4)
        // Then
        assertEquals(0, 1, 2, 3)
    }

    @Test
    fun `Reduce flag`() {
        // Given
        set(1, 2, 3, 0xC)
        // When
        collisions.remove(1, 2, 3, 0x4)
        // Then
        assertEquals(0x8, 1, 2, 3)
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
        set(1, 2, 3, 0x4)
        // When
        val result = collisions.check(1, 2, 3, 0x4)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Flag doesn't collide`() {
        // Given
        set(1, 2, 3, 0x4)
        // When
        val result = collisions.check(1, 2, 3, 0x8)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Copy a rotated chunk`() {
        // Given
        for (i in 0 until 8) {
            set(i, 0, 0, CollisionFlag.NORTH)
        }
        // When
        collisions.copy(Chunk.EMPTY, Chunk.EMPTY, 3)
        // Then
        for (i in 0 until 8) {
            assertEquals(CollisionFlag.WEST, 7, i, 0)
        }
    }

    @Test
    fun `Copy a chunk to another plane`() {
        // Given
        for (i in 0 until 8) {
            set(i, i, 0, CollisionFlag.NORTH_EAST)
        }
        // When
        collisions.copy(Chunk.EMPTY, Chunk(1, 1, 1), 2)
        // Then
        for (i in 0 until 8) {
            assertEquals(CollisionFlag.SOUTH_WEST, 8 + i, 8 + i, 1)
        }
    }

    private fun print(chunk: Chunk) {
        val data = data[chunk.regionPlane.id]!!
        for (y in 7 downTo 0) {
            for (x in 0 until 8) {
                print("${data[((chunk.tile.x + x) * 64) + (chunk.tile.y + y)]} ")
            }
            println()
        }
        println()
    }

    private fun set(x: Int, y: Int, plane: Int, value: Int) {
        collisions[x, y, plane] = value
    }

    private fun assertEquals(expected: Int, x: Int, y: Int, plane: Int) {
        assertEquals(expected, collisions[x, y, plane]) { "x=$x, y=$y, plane=$plane" }
    }
}