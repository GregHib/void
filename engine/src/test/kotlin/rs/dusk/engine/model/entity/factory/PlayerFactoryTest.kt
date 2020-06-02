package rs.dusk.engine.model.entity.factory

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import rs.dusk.engine.data.PlayerLoader
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.visual.player.name
import rs.dusk.engine.model.world.map.collision.collisionModule
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
internal class PlayerFactoryTest : KoinMock() {

    override val modules = listOf(entityFactoryModule, eventBusModule, collisionModule)

    @Test
    fun `Spawn sets index and name`() = runBlocking {
        // Given
        val factory: PlayerFactory = get()
        val player: Player = mockk(relaxed = true)
        every { player.name = any() } just Runs
        val loader: PlayerLoader = declareMock {
            every { loadPlayer("Test") } returns player
        }
        // When
        val result = factory.spawn("Test").await()
        // Then
        assertNotNull(result)
        verifyOrder {
            loader.loadPlayer("Test")
            player.movement.traversal = any()
            player.index = 1
            player.name = "Test"
        }
    }

}