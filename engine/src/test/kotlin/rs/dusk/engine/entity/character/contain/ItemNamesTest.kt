package rs.dusk.engine.entity.character.contain

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.script.KoinMock

internal class ItemNamesTest : KoinMock() {

    private lateinit var container: Container
    private lateinit var itemDefinitions: ItemDefinitions
    private lateinit var decoder: ItemDecoder

    @BeforeEach
    fun setup() {
        container = mockk()
        decoder = mockk()
        every { container.stackable(any()) } returns true
        every { container.indexOf(any()) } returns 0
        every { container.set(any(), any(), any(), any()) } returns true
        every { container.replace(any(), any()) } returns true
        every { container.add(any(), any(), any()) } returns true
        every { container.add(any(), any()) } returns true
        every { container.remove(any(), any(), any()) } returns true
        every { container.remove(any(), any()) } returns true

        itemDefinitions = declare {
            ItemDefinitions(decoder, mapOf("item_name" to mapOf<String, Any>("id" to 1)), mapOf(1 to "item_name"))
        }
    }

    @Test
    fun `Stacking returns true`() {
        assertTrue(container.stackable("item_name"))
    }

    @Test
    fun `Stacking unknown name returns false`() {
        assertFalse(container.stackable("any"))
    }

    @Test
    fun `Contains returns true`() {
        assertTrue(container.contains("item_name"))
    }

    @Test
    fun `Contains returns false`() {
        assertFalse(container.contains("any"))
    }

    @Test
    fun `Index of returns value`() {
        assertEquals(0, container.indexOf("item_name"))
    }

    @Test
    fun `Index of unknown name returns negative one`() {
        assertEquals(-1, container.indexOf("any"))
    }

    @Test
    fun `Set returns true`() {
        assertTrue(container.set(0, "item_name"))
    }

    @Test
    fun `Set unknown name returns false`() {
        assertFalse(container.set(0, "any"))
    }

    @Test
    fun `Replace returns true`() {
        assertTrue(container.replace("item_name", "item_name"))
    }

    @Test
    fun `Replace unknown name returns false`() {
        assertFalse(container.replace("item_name", "any"))
    }

    @Test
    fun `Add returns success`() {
        assertTrue(container.add(0, "item_name"))
        assertTrue(container.add("item_name"))
    }

    @Test
    fun `Add unknown name returns invalid`() {
        assertFalse(container.add(0, "any"))
        assertFalse(container.add("any"))
    }

    @Test
    fun `Remove returns success`() {
        assertTrue(container.remove(0, "item_name"))
        assertTrue(container.remove("item_name"))
    }

    @Test
    fun `Remove unknown name returns invalid`() {
        assertFalse(container.remove(0, "any"))
        assertFalse(container.remove("any"))
    }

}