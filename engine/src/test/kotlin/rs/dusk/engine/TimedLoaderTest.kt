package rs.dusk.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TimedLoaderTest {

    @Test
    fun `Load returns value`() {
        val loader = object : TimedLoader<Int>("Test") {
            override fun load(vararg args: Any?): Int {
                count = 1
                return 42
            }
        }
        val result = loader.load()
        assertEquals(42, result)
    }

    @Test
    fun `Load passes arguments value`() {
        val arguments = arrayOf("One", 2, '3')
        val loader = object : TimedLoader<Int>("Test") {
            override fun load(vararg args: Any?): Int {
                assertTrue(args.contentEquals(arguments))
                return 42
            }
        }
        val result = loader.load(*arguments)
        assertEquals(42, result)
    }

}