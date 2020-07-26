package rs.dusk.engine.client.ui.load

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.client.ui.InterfaceLoader

internal class InterfaceLoaderNameTest {

    private lateinit var loader: InterfaceLoader

    @BeforeEach
    fun setup() {
        loader = InterfaceLoader(mockk())
    }

    @Test
    fun `Load name`() {
        val data = mapOf("interface_name" to mapOf("id" to 1))
        val result = loader.loadNames(data)
        val expected = mapOf("interface_name" to 1)
        assertEquals(expected, result)
    }

    @Test
    fun `Load multiple names`() {
        val data = mapOf(
            "interface_name" to mapOf("id" to 1),
            "interface_name_two" to mapOf("id" to 2)
        )
        val result = loader.loadNames(data)
        val expected = mapOf("interface_name" to 1, "interface_name_two" to 2)
        assertEquals(expected, result)
    }

    @Test
    fun `Missing id throws exception`() {
        val data = mapOf("interface_name" to mapOf<String, Int>())
        assertThrows<IllegalStateException> {
            loader.loadNames(data)
        }
    }
}
