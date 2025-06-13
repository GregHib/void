package world.gregs.voidps.engine.event

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WildcardTest {

    @Test
    fun `Identical strings match`() {
        assertTrue(wildcardEquals("one_two", "one_two"))
    }

    @Test
    fun `Different strings don't match`() {
        assertFalse(wildcardEquals("two_three", "one_two"))
        assertFalse(wildcardEquals("one_two", "two_three"))
        assertFalse(wildcardEquals("one_two", "one_two_three"))
    }

    @Test
    fun `Hash wildcard matches any digit`() {
        for (i in 0 until 10) {
            assertTrue(wildcardEquals("test_#_test", "test_${i}_test"))
        }
    }

    @Test
    fun `Hash matches hash`() {
        assertTrue(wildcardEquals("test_#_test", "test_#_test"))
    }

    @Test
    fun `Match anything`() {
        assertTrue(wildcardEquals("*", "one_two"))
    }

    @Test
    fun `Match starts with`() {
        assertTrue(wildcardEquals("one_*", "one_two"))
        assertTrue(wildcardEquals("one_*", "one_"))
    }

    @Test
    fun `Match ends with`() {
        assertTrue(wildcardEquals("*_two", "one_two"))
        assertTrue(wildcardEquals("*_two", "_two"))
    }

    @Test
    fun `Match ends with repeating character`() {
        assertTrue(wildcardEquals("*_three", "one_thre_three"))
    }

    @Test
    fun `Match contains`() {
        assertTrue(wildcardEquals("*two*", "one_two_three"))
        assertTrue(wildcardEquals("*one*two*three*four*", "_one_two_three_four_"))
    }
}
