package world.gregs.voidps.engine.map.instance

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InstancePoolTest {
    private lateinit var pool: InstancePool

    @BeforeEach
    fun setup() {
        pool = spyk(InstancePool())
    }

    @Test
    fun `Pools instances starting at 93, 3`() {
        // When
        val first = pool.obtain()
        val second = pool.obtain()
        // Then
        assertEquals(93, first.x)
        assertEquals(3, first.y)
        assertEquals(93, second.x)
        assertEquals(4, second.y)
    }

    @Test
    fun `Freed instances are reused`() {
        val first = pool.obtain()
        val second = pool.obtain()
        pool.free(second)
        pool.free(first)
        // When
        val one = pool.obtain()
        val two = pool.obtain()
        // Then
        assertEquals(one, second)
        assertEquals(two, first)
    }

    @Test
    fun `No more instances throws exception`() {
        repeat(159 * 252) {
            pool.obtain()
        }
        // Then
        assertThrows<IllegalStateException> {
            pool.obtain()
        }
    }
}