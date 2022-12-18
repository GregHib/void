package world.gregs.voidps.engine.entity.character.contain.transact

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.remove.ShopItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.item.Item

internal class TransactionTest {

    @Test
    fun `Test index of first`() {
        val container = Container.debug(3)
        val transaction = Transaction(container)

        container.set(0, Item.EMPTY)
        container.set(1, Item.EMPTY)
        container.set(2, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertEquals(2, transaction.indexOfFirst { it!!.isNotEmpty() })
    }

    @Test
    fun `Test that -1 is returned if no non-empty item is found`() {
        val container = Container.debug(3)
        val transaction = Transaction(container)

        container.set(0, Item.EMPTY)
        container.set(1, Item.EMPTY)
        container.set(2, Item.EMPTY)
        assertEquals(-1, transaction.indexOfFirst { it!!.isNotEmpty() })
    }

    @Test
    fun `Test that the correct index is returned for the first empty slot`() {
        val container = Container.debug(3)
        val transaction = Transaction(container)

        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        container.set(1, Item.EMPTY)
        container.set(2, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertEquals(1, transaction.emptyIndex())
    }

    @Test
    fun `Test that -1 is returned if the container is full`() {
        val container = Container.debug(2)
        val transaction = Transaction(container)

        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        container.set(1, Item("orange", 3, def = ItemDefinition.EMPTY))
        container.set(2, Item("pear", 4, def = ItemDefinition.EMPTY))
        assertEquals(-1, transaction.emptyIndex())
    }

    @Test
    fun `Test that the correct item is returned`() {
        val container = Container.debug(5)
        val transaction = Transaction(container)

        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        assertEquals(Item("apple", 1, def = ItemDefinition.EMPTY), transaction.get(0))
    }

    @Test
    fun `Test that empty is returned for an empty slot`() {
        val container = Container.debug(5)
        val transaction = Transaction(container)

        assertEquals(Item.EMPTY, transaction.get(0))
    }

    @Test
    fun `Test that stack-ability is determined correctly based on the container's stack rule`() {
        var container = Container.debug(5, stackRule = AlwaysStack)
        var transaction = Transaction(container)
        assertTrue(transaction.stackable("apple"))

        container = Container.debug(5, stackRule = NeverStack)
        transaction = Transaction(container)
        assertFalse(transaction.stackable("apple"))
    }

    @Test
    fun `Test that removal is allowed based on the container's removal check`() {
        var container = Container.debug(5, removalCheck = DefaultItemRemovalChecker)
        var transaction = Transaction(container)
        assertTrue(transaction.checkRemoval(0, 0))

        container = Container.debug(5, removalCheck = DefaultItemRemovalChecker)
        transaction = Transaction(container)
        assertTrue(transaction.checkRemoval(0, 0))
        assertFalse(transaction.checkRemoval(0, -1))
    }

    @Test
    fun `Test that an item can be set in the current container`() {
        val container = Container.debug(5)
        val transaction = Transaction(container)

        transaction.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        assertEquals(Item("apple", 1, def = ItemDefinition.EMPTY), container.getItem(0))
    }

    @Test
    fun `Test that an item can be set in a different container`() {
        val container = Container.debug(5)
        val transaction = Transaction(container)

        val otherContainer = Container.debug(5)
        transaction.set(otherContainer, 0, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertEquals(Item("banana", 2, def = ItemDefinition.EMPTY), otherContainer.getItem(0))
    }

    @Test
    fun `Test that an invalid item is detected based on the container's valid input`() {
        val container = Container.debug(5)
        container.definitions = mockk(relaxed = true)
        every { container.definitions.contains("apple") } returns true
        every { container.definitions.contains("banana") } returns true
        every { container.definitions.contains("orange") } returns false
        val transaction = Transaction(container)

        assertTrue(transaction.invalid("apple", 0))
        assertFalse(transaction.invalid("apple", 1))
        assertFalse(transaction.invalid("banana", 2))
        assertTrue(transaction.invalid("orange", 3))
    }

    @Test
    fun `Test that an invalid item is detected based on the container's minimum quantity rule`() {
        val container = Container.debug(5, removalCheck = ShopItemRemovalChecker)
        container.definitions = mockk(relaxed = true)
        every { container.definitions.contains("apple") } returns true
        every { container.definitions.contains("banana") } returns true
        every { container.definitions.contains("orange") } returns false
        val transaction = Transaction(container)

        assertTrue(transaction.invalid("apple", -1))
        assertFalse(transaction.invalid("apple", 0))
        assertFalse(transaction.invalid("banana", 2))
        assertTrue(transaction.invalid("orange", 3))
    }

    @Test
    fun `Test that an empty slot is detected as invalid`() {
        val container = Container.debug(5, removalCheck = ShopItemRemovalChecker)
        val transaction = Transaction(container)

        container.set(0, Item.EMPTY)
        assertTrue(transaction.invalid(0))
    }

    @Test
    fun `Test that the history of the transaction container is recorded after being marked`() {
        val container = Container.debug(5)
        val transaction = Transaction(container)

        assertTrue(transaction.marked(container))
    }

    @Test
    fun `Test that the history of a different container is recorded after being marked`() {
        val container = Container.debug(5)
        val otherContainer = Container.debug(5)
        val transaction = Transaction(container)

        transaction.mark(otherContainer)
        assertTrue(transaction.marked(otherContainer))
    }

    @Test
    fun `Test that the history of a container is restored after calling revert`() {
        val container = Container.debug(5)
        val transaction = Transaction(container)

        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        transaction.mark(container)
        container.set(0, Item("banana", 2, def = ItemDefinition.EMPTY))
        transaction.revert()
        assertEquals(Item("apple", 1, def = ItemDefinition.EMPTY), container.getItem(0))
    }

    @Test
    fun `Test that the history of a different container is restored after calling revert`() {
        val container = Container.debug(5)
        val otherContainer = Container.debug(5)
        val transaction = Transaction(container)

        otherContainer.set(0, Item("orange", 3, def = ItemDefinition.EMPTY))
        transaction.mark(otherContainer)
        otherContainer.set(0, Item("pear", 4, def = ItemDefinition.EMPTY))
        transaction.revert()
        assertEquals(Item("orange", 3, def = ItemDefinition.EMPTY), otherContainer.getItem(0))
    }

    @Test
    fun `Test that commit returns false and restores the history if the transaction has failed`() {
        val container = Container.debug(5)
        val transaction = Transaction(container)

        transaction.error = TransactionError.Invalid
        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        transaction.mark(container)
        container.set(0, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertFalse(transaction.commit())
        assertEquals(Item("apple", 1, def = ItemDefinition.EMPTY), container.getItem(0))
    }

    @Test
    fun `Test that commit returns true and clears the history if the transaction is successful`() {
        val container = Container.debug(5)
        val transaction = Transaction(container)

        transaction.error = null
        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        transaction.mark(container)
        container.set(0, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertTrue(transaction.commit())
        assertEquals(Item("banana", 2, def = ItemDefinition.EMPTY), container.getItem(0))
        assertFalse(transaction.marked(container))
    }
}