package world.gregs.voidps.engine.inv.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.MoveItem.move
import world.gregs.voidps.engine.inv.transact.operation.MoveItem.moveAll

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
        assertEquals("item", inventory[0].id)
        assertFalse(transaction.commit())
        assertTrue(inventory[1].isEmpty())
    }

    @Test
    fun `Move item from one index to index in target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.move(0, 1)
        assertTrue(transaction.commit())

        assertTrue(inventory[0].isEmpty())
        assertEquals("item", inventory[1].id)
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
    fun `Move item from one index to index in another inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = inventory(2)
        transaction.move(0, target, 1)
        assertTrue(transaction.commit())

        assertTrue(inventory[0].isEmpty())
        assertEquals("item", target[1].id)
    }

    @Test
    fun `Move item from one index to wrong type target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = inventory(1, stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())

        assertErrorFull(0)
        assertEquals(1, inventory[0].amount)
        assertEquals(1, target[0].amount)
    }

    @Test
    fun `Move non-stackable item from one index to full target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = inventory(2, stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())

        assertErrorFull(0)
        assertEquals(1, inventory[0].amount)
        assertEquals(1, target[0].amount)
        assertEquals(0, target[1].amount)
    }

    @Test
    fun `Move stackable items from one index to target inventory with overflow`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 2)
        }
        val target = inventory(1, stackRule = AlwaysStack) {
            add("item", Int.MAX_VALUE - 1)
        }
        transaction.move(0, target, 0)
        assertFalse(transaction.commit())

        assertErrorFull(1)
        assertEquals(2, inventory[0].amount)
        assertEquals(Int.MAX_VALUE - 1, target[0].amount)
    }

    @Test
    fun `Move stackable items from one index to target inventory`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 5)
        }
        val target = inventory(1, stackRule = AlwaysStack) {
            add("item", 5)
        }
        transaction.move(0, target, 0)
        assertTrue(transaction.commit())

        assertEquals(0, inventory[0].amount)
        assertEquals(10, target[0].amount)
    }

    @Test
    fun `Move from index to free index in target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = inventory(2, stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }
        transaction.move(1, target)
        assertTrue(transaction.commit())

        assertEquals(2, inventory.count)
        assertTrue(inventory[1].isEmpty())
        assertEquals(1, target[1].amount)
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
        assertEquals("item", inventory[0].id)
        assertFalse(transaction.commit())
        assertTrue(inventory[1].isEmpty())
    }

    @Test
    fun `Move non-stackable item to target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = inventory(2, stackRule = NeverStack)
        transaction.move("item", 2, target)
        assertTrue(transaction.commit())

        assertEquals(2, target.count)
        assertEquals(1, target[1].amount)
        assertTrue(inventory[0].isEmpty())
        assertEquals("item", inventory[2].id)
    }

    @Test
    fun `Move non-stackable item to stackable target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = inventory(2, stackRule = AlwaysStack)
        transaction.move("item", 2, target)
        assertTrue(transaction.commit())

        assertEquals(1, target.count)
        assertEquals(2, target[0].amount)
        assertTrue(inventory[0].isEmpty())
        assertEquals("item", inventory[2].id)
    }

    @Test
    fun `Move item to index in target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = inventory(2, stackRule = AlwaysStack)
        transaction.move("item", 2, target, 1)
        assertTrue(transaction.commit())

        assertEquals(1, target.count)
        assertEquals(2, target[1].amount)
        assertTrue(inventory[0].isEmpty())
        assertEquals("item", inventory[2].id)
    }

    @Test
    fun `Move item to index in target inventory with overflow`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 4)
        }
        val target = inventory(2, stackRule = AlwaysStack) {
            set(1, Item("item", Int.MAX_VALUE - 2))
        }
        transaction.move("item", 3, target, 1)
        assertFalse(transaction.commit())
        assertErrorFull(2)
        assertEquals(4, inventory[0].amount)
    }

    @Test
    fun `Move item to index in full target inventory`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 2)
        }
        val target = inventory(2, stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.move("item", 2, target, 1)
        assertFalse(transaction.commit())
        assertErrorFull(1)
        assertEquals(2, inventory[0].amount)
    }

    @Test
    fun `Move item to index in target inventory with wrong type`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = inventory(2, stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }
        transaction.move("item", 1, target, 0)
        assertFalse(transaction.commit())
        assertErrorFull(0)
        assertEquals(1, inventory[0].amount)
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
        val target = inventory(1, stackRule = NeverStack)
        transaction.moveAll(target)
        assertEquals(1, inventory[0].amount)
        assertFalse(transaction.commit())
        assertTrue(target.isEmpty())
    }

    @Test
    fun `Move all non-stackable items to target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
            add("stackable_item", 1)
            add("non_stackable_item", 1)
        }
        val target = inventory(4, stackRule = NeverStack)
        transaction.moveAll(target)
        assertTrue(transaction.commit())

        assertTrue(inventory.isEmpty())
        assertEquals(3, target.count)
    }

    @Test
    fun `Move all non-stackable items to target inventory with insufficient space`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
            add("stackable_item", 1)
            add("non_stackable_item", 1)
        }
        val target = inventory(4, stackRule = NeverStack) {
            add("item", 3)
        }
        transaction.moveAll(target)
        assertFalse(transaction.commit())
        assertErrorFull(0)
    }

    @Test
    fun `Move all stackable items to target inventory`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 4)
            add("stackable_item", 5)
        }
        val target = inventory(2, stackRule = AlwaysStack) {
            add("stackable_item", 5)
        }
        transaction.moveAll(target)
        assertTrue(transaction.commit())

        assertTrue(inventory.isEmpty())
        assertEquals(10, target[0].amount)
        assertEquals(4, target[1].amount)
    }

    @Test
    fun `Move all stackable items to non-stackable target inventory`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 4)
        }
        val target = inventory(5, stackRule = NeverStack)
        transaction.moveAll(target)
        assertTrue(transaction.commit())

        assertTrue(inventory.isEmpty())
        assertEquals(1, target[0].amount)
        assertEquals(1, target[3].amount)
    }

    @Test
    fun `Move all non-stackable items to stackable target inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }
        val target = inventory(1, stackRule = AlwaysStack)
        transaction.moveAll(target)
        assertTrue(transaction.commit())

        assertTrue(inventory.isEmpty())
        assertEquals(4, target[0].amount)
    }

    @Test
    fun `Move all stackable items to target inventory with overflow`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 4)
        }
        val target = inventory(2, stackRule = AlwaysStack) {
            add("item", Int.MAX_VALUE - 2)
        }
        transaction.moveAll(target)
        assertFalse(transaction.commit())

        assertErrorFull(2)
        assertEquals(4, inventory[0].amount)
    }
}
