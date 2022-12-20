package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

internal class MoveItemTest : TransactionOperationTest() {

    /*
        Move index
     */

    @Test
    fun `Move an item from index after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.error = TransactionError.Invalid
        transaction.move(0, 1)
        assertEquals("item", container.getItemId(0))
        assertFalse(transaction.commit())
        assertTrue(container.getItem(1).isEmpty())
    }

    @Test
    fun `Move item from one index to index in target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.move(0, 1)
        assertTrue(transaction.commit())

        assertTrue(container.getItem(0).isEmpty())
        assertEquals("item", container.getItemId(1))
    }

    @Test
    fun `Move invalid index`() {
        transaction.move(-1, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Move item to invalid target index`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.move(0, -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Move invalid empty item`() {
        transaction.move(0, 1)
        assertFalse(transaction.commit())
        assertErrorDeficient(amount = 0)
    }

    @Test
    fun `Move item from one index to index in another container`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = container(2)
        transaction.move(0, target, 1)
        assertTrue(transaction.commit())

        assertTrue(container.getItem(0).isEmpty())
        assertEquals("item", target.getItemId(1))
    }

    @Test
    fun `Move item from one index to wrong type target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = container(1, stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())

        assertErrorFull(0)
        assertEquals(1, container.getAmount(0))
        assertEquals(1, target.getAmount(0))
    }

    @Test
    fun `Move non-stackable item from one index to full target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = container(2, stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())

        assertErrorFull(0)
        assertEquals(1, container.getAmount(0))
        assertEquals(1, target.getAmount(0))
        assertEquals(0, target.getAmount(1))
    }

    @Test
    fun `Move stackable items from one index to target container with overflow`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 2)
        }
        val target = container(1, stackRule = AlwaysStack) {
            add("item", Int.MAX_VALUE - 1)
        }
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())

        assertErrorFull(1)
        assertEquals(2, container.getAmount(0))
        assertEquals(Int.MAX_VALUE - 1, target.getAmount(0))
    }

    @Test
    fun `Move stackable items from one index to target container`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 5)
        }
        val target = container(1, stackRule = AlwaysStack) {
            add("item", 5)
        }
        transaction.move(0, target, 0)
        assertTrue(transaction.commit())

        assertEquals(0, container.getAmount(0))
        assertEquals(10, target.getAmount(0))
    }

    @Test
    fun `Move from index to free index in target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = container(2, stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }
        transaction.move(1, target)
        assertTrue(transaction.commit())

        assertEquals(2, container.count)
        assertTrue(container.getItem(1).isEmpty())
        assertEquals(1, target.getAmount(1))
    }


    /*
        Amount
     */

    @Test
    fun `Move item after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.error = TransactionError.Invalid
        transaction.move("item", amount = 1, toIndex = 1)
        assertEquals("item", container.getItemId(0))
        assertFalse(transaction.commit())
        assertTrue(container.getItem(1).isEmpty())
    }

    @Test
    fun `Move non-stackable item to target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = container(2, stackRule = NeverStack)
        transaction.move("item", 2, target)
        assertTrue(transaction.commit())

        assertEquals(2, target.count)
        assertEquals(1, target.getAmount(1))
        assertTrue(container.getItem(0).isEmpty())
        assertEquals("item", container.getItemId(2))
    }

    @Test
    fun `Move non-stackable item to stackable target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = container(2, stackRule = AlwaysStack)
        transaction.move("item", 2, target)
        assertTrue(transaction.commit())

        assertEquals(1, target.count)
        assertEquals(2, target.getAmount(0))
        assertTrue(container.getItem(0).isEmpty())
        assertEquals("item", container.getItemId(2))
    }

    @Test
    fun `Move item to index in target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = container(2, stackRule = AlwaysStack)
        transaction.move("item", 2, target, 1)
        assertTrue(transaction.commit())

        assertEquals(1, target.count)
        assertEquals(2, target.getAmount(1))
        assertTrue(container.getItem(0).isEmpty())
        assertEquals("item", container.getItemId(2))
    }

    @Test
    fun `Move item to index in target container with overflow`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 4)
        }
        val target = container(2, stackRule = AlwaysStack) {
            set(1, Item("item", Int.MAX_VALUE - 2, def = ItemDefinition.EMPTY))
        }
        transaction.move("item", 3, target, 1)
        assertFalse(transaction.commit())
        assertErrorFull(2)
        assertEquals(4, container.getAmount(0))
    }

    @Test
    fun `Move item to index in full target container`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 2)
        }
        val target = container(2, stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.move("item", 2, target, 1)
        assertFalse(transaction.commit())
        assertErrorFull(1)
        assertEquals(2, container.getAmount(0))
    }

    @Test
    fun `Move item to index in target container with wrong type`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = container(2, stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }
        transaction.move("item", 1, target, 0)
        assertFalse(transaction.commit())
        assertErrorFull(0)
        assertEquals(1, container.getAmount(0))
    }


    /*
        Move all
     */

    @Test
    fun `Move all items after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.error = TransactionError.Invalid
        val target = container(1, stackRule = NeverStack)
        transaction.moveAll(target)
        assertEquals(1, container.getAmount(0))
        assertFalse(transaction.commit())
        assertEquals(0, target.count)
    }

    @Test
    fun `Move all non-stackable items to target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
            add("stackable_item", 1)
            add("non_stackable_item", 1)
        }
        val target = container(4, stackRule = NeverStack)
        transaction.moveAll(target)
        assertTrue(transaction.commit())

        assertEquals(0, container.count)
        assertEquals(3, target.count)
    }

    @Test
    fun `Move all non-stackable items to target container with insufficient space`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
            add("stackable_item", 1)
            add("non_stackable_item", 1)
        }
        val target = container(4, stackRule = NeverStack) {
            add("item", 3)
        }
        transaction.moveAll(target)
        assertFalse(transaction.commit())
        assertErrorFull(0)
    }

    @Test
    fun `Move all stackable items to target container`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 4)
            add("stackable_item", 5)
        }
        val target = container(2, stackRule = AlwaysStack) {
            add("stackable_item", 5)
        }
        transaction.moveAll(target)
        assertTrue(transaction.commit())

        assertEquals(0, container.count)
        assertEquals(10, target.getAmount(0))
        assertEquals(4, target.getAmount(1))
    }

    @Test
    fun `Move all stackable items to non-stackable target container`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 4)
        }
        val target = container(5, stackRule = NeverStack)
        transaction.moveAll(target)
        assertTrue(transaction.commit())

        assertEquals(0, container.count)
        assertEquals(1, target.getAmount(0))
        assertEquals(1, target.getAmount(3))
    }

    @Test
    fun `Move all non-stackable items to stackable target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }
        val target = container(1, stackRule = AlwaysStack)
        transaction.moveAll(target)
        assertTrue(transaction.commit())

        assertEquals(0, container.count)
        assertEquals(4, target.getAmount(0))
    }

    @Test
    fun `Move all stackable items to target container with overflow`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 4)
        }
        val target = container(2, stackRule = AlwaysStack) {
            add("item", Int.MAX_VALUE - 2)
        }
        transaction.moveAll(target)
        assertFalse(transaction.commit())

        assertErrorFull(2)
        assertEquals(4, container.getAmount(0))
    }

}