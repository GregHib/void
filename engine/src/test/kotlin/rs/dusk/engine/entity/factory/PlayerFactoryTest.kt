package rs.dusk.engine.entity.factory

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import rs.dusk.engine.data.PlayerLoader
import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
internal class PlayerFactoryTest : KoinMock() {

    override val modules = listOf(module {
        single { PlayerFactory() }
    }, eventBusModule)

    @Test
    fun `Spawn registers`() = runBlocking {
        // Given
        val factory: PlayerFactory = get()
        val player: Player = mockk(relaxed = true)
        val loader: PlayerLoader = declareMock {
            every { load("Test") } returns player
        }
        val bus: EventBus = declareMock {
            every { emit(any<Registered>()) } just Runs
        }
        // When
        val result = factory.spawn("Test")
        // Then
        assertNotNull(result)
        verifyOrder {
            loader.load("Test")
            player.index = 1
            bus.emit<Registered>(any())
        }
    }

}