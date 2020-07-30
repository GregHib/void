package rs.dusk.engine.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.entity.character.IndexAllocator

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
internal class IndexAllocatorTest {

    lateinit var allocator: IndexAllocator

    @BeforeEach
    fun setup() {
        allocator = IndexAllocator(10)
    }

    @Test
    fun `Obtain empty allocator`() {
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(1, value)
    }

    @Test
    fun `Obtain full allocator`() {
        // Given
        allocator.cap = 10
        // When
        val value = allocator.obtain()
        // Then
        assertNull(value)
    }

    @Test
    fun `Obtain partial allocator`() {
        // Given
        allocator.cap = 5
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(5, value)
    }

    @Test
    fun `Obtain reused allocation`() {
        // Given
        allocator.free.add(5)
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(5, value)
    }

    @Test
    fun `Releasing allocation`() {
        // Given
        allocator.cap = 5
        allocator.release(4)
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(4, value)
    }

    @Test
    fun `Unable to release indices out of bounds`() {
        // Given
        allocator.cap = 5
        // Then
        assertThrows<IllegalArgumentException> {
            allocator.release(6)
        }
    }
}