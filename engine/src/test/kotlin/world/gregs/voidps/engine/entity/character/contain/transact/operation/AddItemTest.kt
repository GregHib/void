package world.gregs.voidps.engine.entity.character.contain.transact.operation

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.ItemStackingRule
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.Transaction
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.script.KoinMock

internal class AddItemTest : KoinMock() {

    private lateinit var container: Container
    private lateinit var transaction: Transaction

    @BeforeEach
    fun setup() {
        declareMock<ItemDefinitions> {
            every { this@declareMock.get(any<String>()) } returns ItemDefinition()
        }
        transaction(AlwaysStack)
    }

    private fun transaction(stackRule: ItemStackingRule) {
        container = Container.debug(capacity = 5, stackRule = stackRule)
        container.definitions = mockk(relaxed = true)
        every { container.definitions.contains("item") } returns true
        transaction = Transaction(container)
    }

    @Test
    fun `Add one stackable item to empty slot`() {
        val id = "item"
        val quantity = 5
        transaction.add(id, quantity)

        assertTrue(transaction.commit())
        assertEquals(quantity, container.getAmount(0))
    }

    @Test
    fun `Add multiple stackable items to existing stack`() {
        val id = "item"
        val initialQuantity = 5
        val quantityToAdd = 3
        transaction.add(id, initialQuantity)
        transaction.add(id, quantityToAdd)

        assertTrue(transaction.commit())
        assertEquals(initialQuantity + quantityToAdd, container.getAmount(0))
    }

    @Test
    fun `Add stackable item to existing stack with overflow`() {
        val id = "item"
        val initialQuantity = 5
        transaction.add(id, initialQuantity)
        transaction.add(id, Int.MAX_VALUE)

        assertFalse(transaction.commit())
        assertOverflow(Int.MAX_VALUE - initialQuantity)
    }

    @Test
    fun `Add one non-stackable item to empty slot`() {
        transaction(NeverStack)
        val id = "item"
        transaction.add(id)
        transaction.commit()

        assertEquals(1, container.getAmount(0))
        assertEquals(1, container.getCount(id).toInt())
    }

    @Test
    fun `Add multiple non-stackable items to empty slots`() {
        transaction(NeverStack)
        val id = "item"
        val quantity = 5
        transaction.add(id, quantity)
        transaction.commit()

        assertEquals(1, container.getAmount(0))
        assertEquals(quantity, container.getCount(id).toInt())
    }

    @Test
    fun `Add multiple non-stackable items to empty slots with insufficient space`() {
        transaction(NeverStack)
        val id = "item"
        val quantity = 10

        transaction.add(id, quantity)
        assertEquals(5, container.getCount(id).toInt())
        assertFalse(transaction.commit())
        assertFullError(5)
    }

    @Test
    fun `Add non-stackable item to full container`() {
        transaction(NeverStack)
        val id = "item"
        val quantity = 5
        repeat(container.capacity) {
            transaction.add(id)
        }
        assertEquals(container.capacity, container.getCount(id).toInt())
        transaction.add(id, quantity)
        assertFalse(transaction.commit())
        assertFullError(0)
    }

    @Test
    fun `Add invalid item to container`() {
        transaction.add("")
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertEquals(0, container.count)
    }

    @Test
    fun `Add invalid quantity of item to container`() {
        transaction.add("item", -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertEquals(0, container.count)
    }

    private fun assertOverflow(remainingSpace: Int) {
        val error = transaction.error
        assertTrue(error is TransactionError.Overflow)
        error as TransactionError.Overflow
        assertEquals(remainingSpace, error.remainingSpace)
    }

    private fun assertFullError(amountAdded: Int) {
        val error = transaction.error
        assertTrue(error is TransactionError.Full)
        error as TransactionError.Full
        assertEquals(amountAdded, error.amountAdded)
    }
}