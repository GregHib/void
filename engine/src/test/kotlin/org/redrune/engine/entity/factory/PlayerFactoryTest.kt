package org.redrune.engine.entity.factory

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import org.redrune.engine.client.IndexAllocator
import org.redrune.engine.data.PlayerLoader
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.Player
import org.redrune.engine.event.EventBus
import org.redrune.engine.event.eventBusModule
import org.redrune.engine.script.KoinMock
import org.redrune.utility.get

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since March 30, 2020
 */
internal class PlayerFactoryTest : KoinMock() {

    @BeforeEach
    fun setup() {
        loadModules(module {
            single { PlayerFactory() }
        }, eventBusModule)
    }

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
        val indexer: IndexAllocator = declareMock {
            every { obtain() } returns 4
        }
        // When
        val result = factory.spawn("Test").await()
        // Then
        assertNotNull(result)
        verifyOrder {
            loader.load("Test")
            indexer.obtain()
            player.index = 4
            bus.emit<Registered>(any())
        }
    }

}