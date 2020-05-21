package rs.dusk.engine.model.entity.factory

import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.mock.declareMock
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
@ExtendWith(MockKExtension::class)
internal class ObjectFactoryTest : KoinMock() {

    override val modules = listOf(entityFactoryModule, cacheDefinitionModule, eventBusModule)

    @Test
    fun `Spawn registers`() {
        // Given
        val factory: ObjectFactory = get()
        val bus: EventBus = declareMock {
            every { emit(any<Registered>()) } just Runs
        }
        declareMock<ObjectDecoder> {
            every { get(any<Int>()) } returns ObjectDefinition(1, sizeX = 1, sizeY = 2)
        }
        // When
        val obj = factory.spawn(1, 10, 20, 1, 0, 2)
        // Then
        assertEquals(1, obj.id)
        verify { bus.emit<Registered>(any()) }
        assertEquals(obj.size, Size(1, 2))
        assertEquals(obj.tile, Tile(10, 20, 1))
    }

}