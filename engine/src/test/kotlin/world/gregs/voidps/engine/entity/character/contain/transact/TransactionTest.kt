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
        container.set(0, Item.EMPTY)
        container.set(1, Item.EMPTY)
        container.set(2, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertEquals(2, container.items.indexOfFirst { it.isNotEmpty() })
    }

    @Test
    fun `Test that -1 is returned if no non-empty item is found`() {
        val container = Container.debug(3)
        val transaction = container.transaction

        container.set(0, Item.EMPTY)
        container.set(1, Item.EMPTY)
        container.set(2, Item.EMPTY)
        assertEquals(-1, container.items.indexOfFirst { it.isNotEmpty() })
    }

    @Test
    fun `Test that the correct index is returned for the first empty slot`() {
        val container = Container.debug(3)

        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        container.set(1, Item.EMPTY)
        container.set(2, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertEquals(1, container.freeIndex())
    }

    @Test
    fun `Test that -1 is returned if the container is full`() {
        val container = Container.debug(2)

        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        container.set(1, Item("orange", 3, def = ItemDefinition.EMPTY))
        container.set(2, Item("pear", 4, def = ItemDefinition.EMPTY))
        assertEquals(-1, container.freeIndex())
    }

    @Test
    fun `Test that the correct item is returned`() {
        val container = Container.debug(5)
        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        assertEquals(Item("apple", 1, def = ItemDefinition.EMPTY), container.getItem(0))
    }

    @Test
    fun `Test that empty is returned for an empty slot`() {
        val container = Container.debug(5)
        assertEquals(Item.EMPTY, container.getItem(0))
    }

    @Test
    fun `Test that stack-ability is determined correctly based on the container's stack rule`() {
        var container = Container.debug(5, stackRule = AlwaysStack)
        assertTrue(container.stackRule.stack("apple"))

        container = Container.debug(5, stackRule = NeverStack)
        assertFalse(container.stackRule.stack("apple"))
    }

    @Test
    fun `Test that removal is allowed based on the container's removal check`() {
        var container = Container.debug(5, removalCheck = DefaultItemRemovalChecker)
        assertTrue(container.removalCheck.shouldRemove(0, 0))

        container = Container.debug(5, removalCheck = DefaultItemRemovalChecker)
        assertTrue(container.removalCheck.shouldRemove(0, 0))
        assertFalse(container.removalCheck.shouldRemove(0, -1))
    }

    @Test
    fun `Test that an item can be set in the current container`() {
        val container = Container.debug(5)
        val transaction = container.transaction

        transaction.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        assertEquals(Item("apple", 1, def = ItemDefinition.EMPTY), container.getItem(0))
    }

    @Test
    fun `Test that an item can be set in a different container`() {
        val container = Container.debug(5)
        val transaction = container.transaction

        val otherContainer = Container.debug(5)
        transaction.linkTransaction(otherContainer).set(0, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertEquals(Item("banana", 2, def = ItemDefinition.EMPTY), otherContainer.getItem(0))
    }

    @Test
    fun `Test that an invalid item is detected based on the container's valid input`() {
        val container = Container.debug(5)
        container.definitions = mockk(relaxed = true)
        every { container.definitions.contains("apple") } returns true
        every { container.definitions.contains("banana") } returns true
        every { container.definitions.contains("orange") } returns false
        val transaction = container.transaction

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
        val transaction = container.transaction

        assertTrue(transaction.invalid("apple", -1))
        assertFalse(transaction.invalid("apple", 0))
        assertFalse(transaction.invalid("banana", 2))
        assertTrue(transaction.invalid("orange", 3))
    }

    @Test
    fun `Test that an empty slot is detected as invalid`() {
        val container = Container.debug(5, removalCheck = ShopItemRemovalChecker)
        val transaction = container.transaction

        container.set(0, Item.EMPTY)
        assertTrue(transaction.invalid(0))
    }

    @Test
    fun `Test that the history of a different container is recorded after being marked`() {
        val container = Container.debug(5)
        val otherContainer = Container.debug(5)
        val transaction = container.transaction

        val otherTransaction = transaction.linkTransaction(otherContainer)
        assertTrue(otherTransaction.state.hasSaved())
    }

    @Test
    fun `Test that the history of a container is restored after calling revert`() {
        val container = Container.debug(5)
        val transaction = container.transaction

        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        transaction.linkTransaction(container)
        container.set(0, Item("banana", 2, def = ItemDefinition.EMPTY))
        transaction.revert()
        assertEquals(Item("apple", 1, def = ItemDefinition.EMPTY), container.getItem(0))
    }

    @Test
    fun `Test that the history of a different container is restored after calling revert`() {
        val container = Container.debug(5)
        val otherContainer = Container.debug(5)
        val transaction = container.transaction

        otherContainer.set(0, Item("orange", 3, def = ItemDefinition.EMPTY))
        transaction.linkTransaction(otherContainer)
        otherContainer.set(0, Item("pear", 4, def = ItemDefinition.EMPTY))
        transaction.revert()
        assertEquals(Item("orange", 3, def = ItemDefinition.EMPTY), otherContainer.getItem(0))
    }

    @Test
    fun `Test that commit returns false and restores the history if the transaction has failed`() {
        val container = Container.debug(5)
        val transaction = container.transaction
        transaction.start()
        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        transaction.commit()
        assertTrue(transaction.commit())

        transaction.error = TransactionError.Invalid
        container.set(0, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertFalse(transaction.commit())
        assertEquals(Item("apple", 1, def = ItemDefinition.EMPTY), container.getItem(0))
    }

    @Test
    fun `Test that commit returns true and clears the history if the transaction is successful`() {
        val container = Container.debug(5)
        val transaction = container.transaction

        transaction.error = null
        container.set(0, Item("apple", 1, def = ItemDefinition.EMPTY))
        transaction.linkTransaction(container)
        container.set(0, Item("banana", 2, def = ItemDefinition.EMPTY))
        assertTrue(transaction.commit())
        assertEquals(Item("banana", 2, def = ItemDefinition.EMPTY), container.getItem(0))
        assertFalse(transaction.state.hasSaved())
    }
}