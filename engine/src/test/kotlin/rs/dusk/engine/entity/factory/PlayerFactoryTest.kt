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
import rs.dusk.engine.entity.model.visual.visuals.player.name
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
        every { player.name = any() } just Runs
        val loader: PlayerLoader = declareMock {
            every { loadPlayer("Test") } returns player
        }
        val bus: EventBus = declareMock {
            every { emit(any<Registered>()) } just Runs
        }
        // When
        val result = factory.spawn("Test").await()
        // Then
        assertNotNull(result)
        verifyOrder {
            loader.loadPlayer("Test")
            player.index = 1
            player.name = "Test"
            bus.emit<Registered>(any())
        }
    }

}