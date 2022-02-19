package world.gregs.voidps.engine.map.collision

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.region.RegionPlane

internal class CollisionsTest {

    lateinit var regions: IntArray
    lateinit var data: Array<IntArray?>
    lateinit var collisions: Collisions

    @BeforeEach
    fun setup() {
        val id = RegionPlane.getId(0, 0, 3)
        regions = IntArray(id + 1) { -1 }
        regions[id] = 0
        data = arrayOfNulls(2)
        collisions = spyk(Collisions(regions, data))
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

    private fun set(x: Int, y: Int, plane: Int, value: Int) {
        val index = x * 64 + y
        val id = RegionPlane.getId(0, 0, plane)
        if (data[regions[id]] == null) {
            data[regions[id]] = IntArray(4096)
        }
        data[regions[id]]!![index] = value
    }

    private fun assertEquals(expected: Int, x: Int, y: Int, plane: Int) {
        val index = x * 64 + y
        val id = RegionPlane.getId(0, 0, plane)
        assertEquals(expected, data[regions[id]]!![index])
    }
}