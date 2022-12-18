package world.gregs.voidps.engine.entity.character.contain.transact.operation

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.ItemStackingRule
import world.gregs.voidps.engine.entity.character.contain.transact.Transaction
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.script.KoinMock

abstract class TransactionOperationTestBase : KoinMock() {

    protected lateinit var container: Container
    protected lateinit var transaction: Transaction

    @BeforeEach
    fun setup() {
        declareMock<ItemDefinitions> {
            every { this@declareMock.get(any<String>()) } returns ItemDefinition()
        }
        transaction()
    }

    protected fun transaction(capacity: Int = 5, stackRule: ItemStackingRule = AlwaysStack, block: Transaction.() -> Unit = {}) {
        container = container(capacity, stackRule, block)
        transaction = container.transaction
        transaction.start()
    }

    protected fun container(capacity: Int = 5, stackRule: ItemStackingRule = AlwaysStack, block: (Transaction.() -> Unit)? = null): Container {
        val container = Container.debug(capacity = capacity, stackRule = stackRule)
        container.definitions = mockk(relaxed = true)
        every { container.definitions.contains("item") } returns true
        every { container.definitions.contains("stackable_item") } returns true
        every { container.definitions.contains("non_stackable_item") } returns true
        val transaction = container.transaction
        if (block != null) {
            transaction.start()
            block.invoke(transaction)
            assertTrue(transaction.commit())
        }
        return container
    }

    protected fun assertDeficient(amountRemoved: Int) {
        val error = transaction.error
        assertTrue(error is TransactionError.Deficient)
        error as TransactionError.Deficient
        Assertions.assertEquals(amountRemoved, error.amountRemoved)
    }

    protected fun assertUnderflow(quantity: Int) {
        val error = transaction.error
        assertTrue(error is TransactionError.Underflow)
        error as TransactionError.Underflow
        Assertions.assertEquals(quantity, error.quantity)
    }

    protected fun assertOverflow(remainingSpace: Int) {
        val error = transaction.error
        assertTrue(error is TransactionError.Overflow)
        error as TransactionError.Overflow
        Assertions.assertEquals(remainingSpace, error.remainingSpace)
    }

    protected fun assertFull(amountAdded: Int) {
        val error = transaction.error
        assertTrue(error is TransactionError.Full)
        error as TransactionError.Full
        Assertions.assertEquals(amountAdded, error.amountAdded)
    }
}