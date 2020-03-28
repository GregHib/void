package org.redrune.engine.entity.tile

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.get
import org.koin.test.mock.declareMock
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.Player
import org.redrune.engine.event.EventBus
import org.redrune.engine.event.eventBusModule
import org.redrune.engine.model.Tile
import org.redrune.engine.script.ScriptMock

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since March 30, 2020
 */
@ExtendWith(MockKExtension::class)
internal class TileSystemTest : ScriptMock() {

    @BeforeEach
    override fun setup() {
        loadModules(eventBusModule)
        setProperty("homeX", 10)
        setProperty("homeY", 20)
        setProperty("homePlane", 2)
        super.setup()
    }

    @Test
    fun `New player is set to home tile`() {
        // Given
        val bus: EventBus = get()
        val entity = mockk<Player>(relaxed = true)
        val tiles = declareMock<Tiles> {
            every { contains(entity) } returns false
            every { set(entity, any()) } just Runs
        }
        // When
        bus.emit(Registered(entity))
        // Then
        verify { tiles[entity] = Tile(10, 20, 2) }// FIXME https://github.com/mockk/mockk/issues/152
    }

    @Test
    fun `Existing player isn't set to home tile`() {
        // Given
        val bus: EventBus = get()
        val entity = mockk<Player>(relaxed = true)
        val tiles = declareMock<Tiles> {
            every { contains(entity) } returns true
            every { set(entity, any()) } just Runs
        }
        // When
        bus.emit(Registered(entity))
        // Then
        verify(exactly = 0) { tiles[entity] = any() }// FIXME https://github.com/mockk/mockk/issues/152
    }

}