package world.gregs.voidps.engine.contain.transact

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.entity.item.Item

internal class TransactionControllerTest {

    private lateinit var container: Container
    private lateinit var controller: TransactionController

    @BeforeEach
    fun setup() {
        container = Container.debug(1)
        controller = container.transaction
    }

    @Test
    fun `Start resets transaction and saves container state`() {
        controller.error = TransactionError.Invalid

        controller.start()

        assertEquals(TransactionError.None, controller.error)
        assertTrue(controller.state.hasSaved())
    }

    @Test
    fun `Revert transaction`() {
        controller.start()
        container.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        assertTrue(controller.revert())
        assertTrue(container[0].isEmpty())
    }

    @Test
    fun `Revert linked transactions`() {
        controller.start()
        val otherContainer = Container.debug(1)
        otherContainer.transaction.start()

        controller.link(otherContainer.transaction)
        otherContainer.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))

        assertTrue(controller.revert())
        assertTrue(otherContainer[0].isEmpty())
    }

    @Test
    fun `Revert failed if no saved container state`() {
        container.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))

        assertFalse(controller.revert())
        assertEquals("item", container[0].id)
    }

    @Test
    fun `Revert continues for linked transactions even if failed`() {
        container.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))

        val otherContainer = Container.debug(1)
        otherContainer.transaction.start()
        controller.link(otherContainer.transaction)
        otherContainer.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))

        assertFalse(controller.revert())
        assertEquals("item", container[0].id)
        assertTrue(otherContainer[0].isEmpty())
    }

    @Test
    fun `Commit transaction`() {
        controller.start()
        container.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        assertTrue(controller.commit())
        assertEquals("item", container[0].id)
    }

    @Test
    fun `Commit linked transaction`() {
        val otherContainer = Container.debug(1)
        controller.start()
        otherContainer.transaction.start()

        controller.link(otherContainer.transaction)

        container.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        otherContainer.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        assertTrue(controller.commit())

        assertEquals("item", container[0].id)
        assertEquals("item", otherContainer[0].id)
    }

    @Test
    fun `Commit failed transaction`() {
        controller.start()
        container.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        controller.error = TransactionError.Invalid
        assertFalse(controller.commit())

        // Check both containers were reverted
        assertTrue(container[0].isEmpty())
    }

    @Test
    fun `Commit failed linked transaction`() {
        val otherContainer = Container.debug(1)
        controller.start()
        otherContainer.transaction.start()

        controller.link(otherContainer.transaction)

        container.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        otherContainer.data.items = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        otherContainer.transaction.error = TransactionError.Invalid
        assertFalse(controller.commit())

        // Check both containers were reverted
        assertTrue(container[0].isEmpty())
        assertTrue(otherContainer[0].isEmpty())
    }
}