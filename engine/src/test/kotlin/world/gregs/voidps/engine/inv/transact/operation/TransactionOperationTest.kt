package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.inv.remove.ItemRemovalChecker
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.inv.restrict.NoRestrictions
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.ItemStackingRule
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.script.KoinMock

abstract class TransactionOperationTest : KoinMock() {

    protected lateinit var inventory: Inventory
    protected lateinit var transaction: Transaction
    val normalStackRule = object : ItemStackingRule {
        override fun stackable(id: String): Boolean {
            return id == "stackable_item"
        }
    }
    val validItems = object : ItemRestrictionRule {
        override fun restricted(id: String): Boolean {
            return id.isBlank() || id == "invalid_item"
        }
    }
    protected lateinit var itemDefinitions: ItemDefinitions

    @BeforeEach
    fun setup() {
        itemDefinitions = declareMock<ItemDefinitions> {
            every { this@declareMock.get(any<String>()) } returns ItemDefinition()
            every { this@declareMock.getOrNull(any<String>()) } returns ItemDefinition()
        }
        transaction()
    }

    protected fun transaction(
        capacity: Int = 5,
        stackRule: ItemStackingRule = AlwaysStack,
        itemRule: ItemRestrictionRule = NoRestrictions,
        removalCheck: ItemRemovalChecker = DefaultItemRemovalChecker,
        block: Transaction.() -> Unit = {}
    ) {
        inventory = inventory(capacity, stackRule, itemRule, removalCheck, block)
        transaction = inventory.transaction
        transaction.start()
    }

    protected fun inventory(
        capacity: Int = 5,
        stackRule: ItemStackingRule = AlwaysStack,
        itemRule: ItemRestrictionRule = NoRestrictions,
        removalCheck: ItemRemovalChecker = DefaultItemRemovalChecker,
        block: (Transaction.() -> Unit)? = null
    ): Inventory {
        val inventory = Inventory.debug(
            capacity = capacity,
            stackRule = stackRule,
            itemRule = itemRule,
            removalCheck = removalCheck
        )
        val transaction = inventory.transaction
        if (block != null) {
            transaction.start()
            block.invoke(transaction)
            assertTrue(transaction.commit())
        }
        return inventory
    }

    protected fun assertErrorDeficient(amount: Int) {
        val error = transaction.error
        assertTrue(error is TransactionError.Deficient) { "Expected TransactionError.Deficient, Found $error" }
        error as TransactionError.Deficient
        assertEquals(amount, error.amount)
    }

    protected fun assertErrorFull(amount: Int) {
        val error = transaction.error
        assertTrue(error is TransactionError.Full) { "Expected TransactionError.Full, Found $error" }
        error as TransactionError.Full
        assertEquals(amount, error.amount)
    }
}