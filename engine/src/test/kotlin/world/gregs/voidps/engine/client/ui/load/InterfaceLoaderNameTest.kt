package world.gregs.voidps.engine.client.ui.load

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetailsLoader

internal class InterfaceLoaderNameTest {

    private lateinit var loader: InterfaceDetailsLoader

    @BeforeEach
    fun setup() {
        loader = InterfaceDetailsLoader(mockk())
    }

    @Test
    fun `Load name`() {
        val data = mapOf("interface_name" to mapOf("id" to 1))
        val result = loader.loadNames(data)
        val expected = mapOf(1 to "interface_name")
        assertEquals(expected, result)
    }

    @Test
    fun `Load multiple names`() {
        val data = mapOf(
            "interface_name" to mapOf("id" to 1),
            "interface_name_two" to mapOf("id" to 2)
        )
        val result = loader.loadNames(data)
        val expected = mapOf(1 to "interface_name", 2 to "interface_name_two")
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
