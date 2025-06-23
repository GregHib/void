package world.gregs.voidps.engine.client.ui.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getHash
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getIndices

internal class InterfaceOptionSettingsTest {

    @Test
    fun `Hash single option index`() {
        assertEquals(2, getHash(0))
        assertEquals(4, getHash(1))
        assertEquals(8, getHash(2))
        assertEquals(16, getHash(3))
    }

    @Test
    fun `Hash multiple inventory option indices`() {
        assertEquals(6, getHash(0, 1))
        assertEquals(18, getHash(0, 3))
        assertEquals(14, getHash(0, 1, 2))
        assertEquals(30, getHash(0, 1, 2, 3))
        assertEquals(60, getHash(1, 2, 99, 100))
        assertEquals(11382, getHash(0, 1, 3, 4, 5, 9, 10, 12))
    }

    @Test
    fun `Get active option index`() {
        assertEquals(emptyList<Int>(), getIndices(0))
        assertEquals(listOf(0), getIndices(2))
        assertEquals(listOf(1), getIndices(4))
        assertEquals(listOf(2), getIndices(8))
        assertEquals(listOf(3), getIndices(16))
    }

    @Test
    fun `Get active option indices`() {
        assertEquals(listOf(0, 1), getIndices(6))
        assertEquals(listOf(0, 1, 2), getIndices(14))
        assertEquals(listOf(0, 1, 2, 3), getIndices(30))
        assertEquals(listOf(0, 3), getIndices(18))
        assertEquals(listOf(0, 1, 3, 4, 5, 9, 10, 12), getIndices(11382))
    }
}
