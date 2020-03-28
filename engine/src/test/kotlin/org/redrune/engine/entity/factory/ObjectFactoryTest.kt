package org.redrune.engine.entity.factory

import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.Entity
import org.redrune.engine.entity.tile.Tiles
import org.redrune.engine.entity.tile.tileModule
import org.redrune.engine.event.EventBus
import org.redrune.engine.event.eventBusModule
import org.redrune.engine.model.Tile
import org.redrune.engine.script.KoinMock
import org.redrune.utility.get

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since March 30, 2020
 */
@ExtendWith(MockKExtension::class)
internal class ObjectFactoryTest : KoinMock() {

    @BeforeEach
    fun setup() {
        loadModules(module { single { ObjectFactory() } }, eventBusModule, tileModule)
    }

    @Test
    fun `Spawn registers`() {
        // Given
        val factory: ObjectFactory = get()
        val bus: EventBus = declareMock {
            every { emit(any<Registered>()) } just Runs
        }
        val tiles: Tiles = declareMock {
            every { this@declareMock[any<Entity>()] = any<Tile>() } just Runs// FIXME https://github.com/mockk/mockk/issues/152
        }
        // When
        val obj = factory.spawn(1, 10, 20, 1, 2)
        // Then
        assertEquals(1, obj.id)
        verify { bus.emit<Registered>(any()) }
        verify { tiles[obj] = eq(Tile(10, 20, 1)) }
    }

}