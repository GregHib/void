package world.gregs.voidps.engine.map.instance

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InstancesTest {

    @BeforeEach
    fun setup() {
        Instances.reset()
    }

    @Test
    fun `Small instances starting at 101, 1`() {
        // When
        val first = Instances.small()
        val second = Instances.small()
        // Then
        assertEquals(101, first.x)
        assertEquals(1, first.y)
        assertEquals(101, second.x)
        assertEquals(4, second.y)
    }

    @Test
    fun `Large instances starting at 101, 83`() {
        // When
        val first = Instances.large()
        val second = Instances.large()
        // Then
        assertEquals(101, first.x)
        assertEquals(83, first.y)
        assertEquals(101, second.x)
        assertEquals(89, second.y)
    }

    @Test
    fun `Freed instances are reused`() {
        val first = Instances.small()
        val second = Instances.small()
        Instances.free(second)
        Instances.free(first)
        repeat(1375) {
            Instances.small()
        }
        // When
        val one = Instances.small()
        val two = Instances.small()
        // Then
        assertEquals(second, one)
        assertEquals(first, two)
    }

    @Test
    fun `No more instances throws exception`() {
        repeat(1377) {
            Instances.small()
        }
        // Then
        assertThrows<NullPointerException> {
            Instances.small()
        }
    }
}
