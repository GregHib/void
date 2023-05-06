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
    fun `Small instances starting at 101, 1`() {
        // When
        val first = pool.small()
        val second = pool.small()
        // Then
        assertEquals(101, first.x)
        assertEquals(1, first.y)
        assertEquals(101, second.x)
        assertEquals(4, second.y)
    }

    @Test
    fun `Large instances starting at 101, 83`() {
        // When
        val first = pool.large()
        val second = pool.large()
        // Then
        assertEquals(101, first.x)
        assertEquals(83, first.y)
        assertEquals(101, second.x)
        assertEquals(89, second.y)
    }

    @Test
    fun `Freed instances are reused`() {
        val first = pool.small()
        val second = pool.small()
        pool.free(second)
        pool.free(first)
        repeat(1375) {
            pool.small()
        }
        // When
        val one = pool.small()
        val two = pool.small()
        // Then
        assertEquals(second, one)
        assertEquals(first, two)
    }

    @Test
    fun `No more instances throws exception`() {
        repeat(1377) {
            pool.small()
        }
        // Then
        assertThrows<NullPointerException> {
            pool.small()
        }
    }
}