package rs.dusk.engine.entity.item.detail

import com.google.common.collect.HashBiMap
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.ContainerResult
import rs.dusk.engine.script.KoinMock

internal class ItemNamesTest : KoinMock() {

    private lateinit var container: Container
    private lateinit var itemDetails: ItemDetails

    @BeforeEach
    fun setup() {
        container = mockk()
        every { container.stackable(any()) } returns true
        every { container.indexOf(any()) } returns 0
        every { container.set(any(), any(), any(), any()) } returns true
        every { container.replace(any(), any()) } returns true
        every { container.add(any(), any(), any()) } returns ContainerResult.Addition.Added
        every { container.add(any(), any()) } returns ContainerResult.Addition.Added
        every { container.remove(any(), any(), any()) } returns ContainerResult.Removal.Removed
        every { container.remove(any(), any()) } returns ContainerResult.Removal.Removed

        itemDetails = declare {
            ItemDetails(mapOf(1 to ItemDetail(1)), HashBiMap.create(mapOf(1 to "item_name")))
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
        assertEquals(ContainerResult.Addition.Added, container.add(0, "item_name"))
        assertEquals(ContainerResult.Addition.Added, container.add("item_name"))
    }

    @Test
    fun `Add unknown name returns invalid`() {
        assertEquals(ContainerResult.Addition.Failure.Invalid, container.add(0, "any"))
        assertEquals(ContainerResult.Addition.Failure.Invalid, container.add("any"))
    }

    @Test
    fun `Remove returns success`() {
        assertEquals(ContainerResult.Removal.Removed, container.remove(0, "item_name"))
        assertEquals(ContainerResult.Removal.Removed, container.remove("item_name"))
    }

    @Test
    fun `Remove unknown name returns invalid`() {
        assertEquals(ContainerResult.Removal.Failure.Invalid, container.remove(0, "any"))
        assertEquals(ContainerResult.Removal.Failure.Invalid, container.remove("any"))
    }

}