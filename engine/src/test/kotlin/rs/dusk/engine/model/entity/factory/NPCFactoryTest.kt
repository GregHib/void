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
import rs.dusk.cache.definition.data.NPCDefinition
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.entity.Direction
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
internal class NPCFactoryTest : KoinMock() {

    override val modules = listOf(cacheDefinitionModule, entityFactoryModule, eventBusModule)

    @Test
    fun `Spawn registers`() {
        // Given
        val factory: NPCFactory = get()
        val bus: EventBus = declareMock {
            every { emit(any<Registered>()) } just Runs
        }
        declareMock<NPCDecoder> {
            every { get(any<Int>()) } returns NPCDefinition(id = 1, size = 2)
        }
        // When
        val npc = factory.spawn(1, 10, 20, 1, Direction.NONE)!!
        // Then
        assertEquals(1, npc.id)
        verify { bus.emit<Registered>(any()) }
        assertEquals(npc.size, Size(2, 2))
        assertEquals(npc.tile, Tile(10, 20, 1))
    }

}