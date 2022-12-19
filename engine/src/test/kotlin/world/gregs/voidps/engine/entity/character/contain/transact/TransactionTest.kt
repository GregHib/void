package world.gregs.voidps.engine.entity.character.contain.transact

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.transact.operation.TransactionOperationTestBase
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransactionTest : TransactionOperationTestBase() {

    @Test
    fun `Set tracks changes`() {
        val container = Container.debug(1)
        val events: Events = mockk(relaxed = true)
        container.events.add(events)
        val transaction = container.transaction
        transaction.set(0, Item("item", 1, def = ItemDefinition.EMPTY))
        transaction.changes.send()
        verify { events.emit(any<ItemChanged>()) }
    }


    @Test
    fun `Link second container to transaction`() {
        val container = Container.debug(1)
        val transaction = container.transaction
        val container2 = Container.debug(1)
        transaction.start()

        assertFalse(container2.transaction.state.hasSaved())
        val transaction2 = transaction.linkTransaction(container2)
        assertTrue(container2.transaction.state.hasSaved())

        transaction2.error = TransactionError.Invalid
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Can't link with container in a transaction`() {
        val container = Container.debug(1)
        val transaction = container.transaction
        val container2 = Container.debug(1)
        val transaction2 = container2.transaction
        transaction2.start()
        transaction.linkTransaction(container2)
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Can't link transaction with itself`() {
        val container = Container.debug(1)
        val transaction = container.transaction
        transaction.linkTransaction(container)
        assertEquals(TransactionError.Invalid, transaction.error)
    }
}