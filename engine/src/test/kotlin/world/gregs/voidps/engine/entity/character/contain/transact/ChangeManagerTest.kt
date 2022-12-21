package world.gregs.voidps.engine.entity.character.contain.transact

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ContainerUpdate
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
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
        change.bind(events)
        change.track("container", 1, Item.EMPTY, "target", Item("item", 1, def = ItemDefinition.EMPTY))
        change.send()
        verify {
            events.emit(any<ItemChanged>())
            events.emit(any<ContainerUpdate>())
        }
    }

    @Test
    fun `Clear tracked changes`() {
        val events = mockk<Events>(relaxed = true)
        change.bind(events)
        change.track("container", 1, Item.EMPTY, "target", Item("item", 1, def = ItemDefinition.EMPTY))
        change.clear()
        change.send()
        verify(exactly = 0) { events.emit(any()) }
    }


}