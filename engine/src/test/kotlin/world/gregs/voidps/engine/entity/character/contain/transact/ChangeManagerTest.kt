package world.gregs.voidps.engine.entity.character.contain.transact

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events

internal class ChangeManagerTest {

    private lateinit var change: ChangeManager
    private lateinit var container: Container

    @BeforeEach
    fun setup() {
        container = Container.debug(1)
        change = ChangeManager(container)
    }

    @Test
    fun `Track and send changes`() {
        val events = mockk<Events>(relaxed = true)
        container.events.add(events)
        change.track(1, Item.EMPTY, Item("item", 1, def = ItemDefinition.EMPTY), moved = false)
        change.send()
        verify(exactly = 1) { events.emit(any()) }
    }

    @Test
    fun `Clear tracked changes`() {
        val events = mockk<Events>(relaxed = true)
        container.events.add(events)
        change.track(1, Item.EMPTY, Item("item", 1, def = ItemDefinition.EMPTY), moved = false)
        change.clear()
        change.send()
        verify(exactly = 0) { events.emit(any()) }
    }


}