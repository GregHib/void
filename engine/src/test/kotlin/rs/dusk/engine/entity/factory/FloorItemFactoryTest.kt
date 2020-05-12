package rs.dusk.engine.entity.factory

import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
@ExtendWith(MockKExtension::class)
internal class FloorItemFactoryTest : KoinMock() {

    override val modules = listOf(module { single { FloorItemFactory() } }, eventBusModule)

    @Test
    fun `Spawn registers`() {
        // Given
        val factory: FloorItemFactory = get()
        val bus: EventBus = declareMock {
            every { emit(any<Registered>()) } just Runs
        }
        // When
        val item = factory.spawn(1, 10, 20, 1)
        // Then
        assertEquals(1, item.id)
        verify { bus.emit<Registered>(any()) }
        assertEquals(item.tile, Tile(10, 20, 1))
    }

}