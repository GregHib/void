package world.gregs.voidps.engine.inv.transact

import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.InventoryApi

internal class ChangeManagerTest {

    private lateinit var change: ChangeManager
    private lateinit var inventory: Inventory

    @BeforeEach
    fun setup() {
        inventory = Inventory.debug(1)
        change = ChangeManager(inventory)
        mockkObject(InventoryApi)
    }

    @AfterEach
    fun teardown() {
        unmockkObject(InventoryApi)
    }

    @Test
    fun `Track and send changes`() {
        val events = mockk<Player>(relaxed = true)
        change.bind(events)
        change.track(from = "inventory", index = 1, previous = Item.EMPTY, fromIndex = 1, item = Item("item", 1))
        change.send()
        verify {
            InventoryApi.changed(events, any())
            InventoryApi.update(events, any(), any())
        }
    }

    @Test
    fun `Clear tracked changes`() {
        val events = mockk<Player>(relaxed = true)
        change.bind(events)
        change.track("inventory", 1, Item.EMPTY, 1, Item("item", 1))
        change.clear()
        change.send()
        verify(exactly = 0) { InventoryApi.changed(events, any()) }
    }
}
