package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class MoveItemTest : TransactionOperationTestBase() {
    @Test
    fun `Move a non-stackable item from one index to another container`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = container(capacity = 1, stackRule = NeverStack)

        // Move the item from the current container to the target container
        transaction.move(0, target)
        println(transaction.error)
        assertTrue(transaction.commit())

        // Assert that the item was moved to the target container
        assertEquals("item", target.getItemId(0))
        assertTrue(container.getItem(0).isEmpty())
    }

    @Test
    fun `Move a non-stackable item from one index to a full target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        // Fill the target container
        val target = container(capacity = 1, stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }

        // Attempt to move the item to the target container
        transaction.move(0, target)
        assertFalse(transaction.commit())

        // Assert that the item was not moved to the target container
        assertEquals("item", container.getItemId(0))
        assertEquals("non_stackable_item", target.getItemId(0))
    }

    @Test
    fun `Move a non-stackable item to an occupied index within the same container`() {
        // Add two items to the container
        transaction(stackRule = NeverStack) {
            add("item", 1)
            add("non_stackable_item", 1)
        }

        // Move the first item to the index of the second item
        transaction.move(0, container, 1)
        assertFalse(transaction.commit())

        // Assert that the items have not been swapped
        assertEquals("item", container.getItemId(0))
        assertEquals("non_stackable_item", container.getItemId(1))
    }

    @Test
    fun `Move a non-stackable item from one index to a free space within the same container`() {
        // Add an item to the container
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }

        // Move the item to a different index
        transaction.move(0, 1)
        assertTrue(transaction.commit())

        // Assert that the item has been moved to the correct index
        assertEquals(0, container.getAmount(0))
        assertEquals(1, container.getAmount(1))
    }

    @Test
    fun `Move a non-stackable item from one container to another at index`() {
        // Add an item to the container
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }

        // Create the target container
        val target = container(capacity = 1, stackRule = NeverStack)

        // Move the item to the target container
        transaction.move(0, target, 0)
        assertTrue(transaction.commit())

        // Assert that the item was moved to the target container
        assertEquals(0, container.getAmount(0))
        // Assert that the item was added to the second container
        assertEquals(1, target.getAmount(0))
    }

    @Test
    fun `Move a non-stackable item from one container to another index with a full target container`() {
        // Add an item to the container
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }

        // Create the target container
        val target = container(capacity = 1, stackRule = NeverStack) {
            add("item", 1)
        }

        // Attempt to move the item to the full target container
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())

        // Assert that the item was not moved to the target container
        assertFull(0)
        assertEquals(1, container.getAmount(0))
        assertEquals(1, target.getAmount(0))
    }

    @Test
    fun `Move a non-stackable item from one container to another with an index out of bounds`() {
        // Add an item to the container
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }

        // Create the target container
        val target = container(capacity = 1, stackRule = NeverStack)

        // Attempt to move the item to the target container at an index out of bounds
        transaction.move(0, target, 2)
        assertFalse(transaction.commit())

        // Assert that the item was not moved to the target container
        assertEquals(1, container.getAmount(0))
        assertEquals(0, target.getAmount(0))
    }

    @Test
    fun `Move a stackable item to another container`() {
        // Add an item to the container
        transaction {
            add("item", 2)
        }

        // Create a second container
        val target = container(capacity = 5, stackRule = AlwaysStack)

        // Move the item from the container to the second container
        transaction.move(0, target, 0)
        assertTrue(transaction.commit())

        // Assert that the item was removed from the container
        assertEquals(0, container.getAmount(0))
        // Assert that the item was added to the second container
        assertEquals(2, target.getAmount(0))
    }

    @Test
    fun `Move a stackable item to a full container`() {
        // Add an item to the container
        transaction {
            add("item", 2)
        }

        // Create a second container that is already full
        val target = container(capacity = 1, stackRule = AlwaysStack) {
            add("stackable_item", 1)
        }

        // Attempt to move the item from the container to the second container
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())
        assertFull(0)
    }

    @Test
    fun `Move a stackable item to an overflowing container`() {
        // Add an item to the container
        transaction {
            add("item", 2)
        }

        // Create a second container that is already full
        val target = container(capacity = 2, stackRule = AlwaysStack) {
            add("item", Int.MAX_VALUE - 1)
        }

        // Attempt to move the item from the container to the second container
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())
        assertOverflow(1)
    }

    @Test
    fun `Move a specific quantity of an item from the current container to another container`() {
        transaction {
            add("item", 3)
        }

        // Create the target container
        val target = container(3)

        // Move 2 of the items from the current container to the target container
        transaction.move("item", 2, target)
        assertTrue(transaction.commit())

        // Assert that 2 of the items were removed from the current container
        assertEquals(1, container.getAmount(0))
        // Assert that the 2 items were added to the target container
        assertEquals(2, target.getAmount(0))
    }

    @Test
    fun `Move an item after the transaction has failed`() {
        // Add an item to container
        transaction(1) {
            add("item", 1)
        }

        val container2 = container(1, stackRule = NeverStack)

        // Set the transaction to failed
        transaction.error(TransactionError.Full(0))

        // Attempt to move the item to container2
        transaction.move(0, container2)

        // Assert that the item was not moved to container2
        assertEquals(0, container2.getCount("item").toInt())

        // Assert that the item is still in container1
        assertEquals(1, container.getCount("item").toInt())
    }
}