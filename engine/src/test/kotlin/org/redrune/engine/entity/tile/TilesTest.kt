package org.redrune.engine.entity.tile

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.mock.declareMock
import org.redrune.engine.entity.model.Entity
import org.redrune.engine.event.eventBusModule
import org.redrune.engine.model.Tile
import org.redrune.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since March 30, 2020
 */
@ExtendWith(MockKExtension::class)
internal class TilesTest : KoinMock() {

    @BeforeEach
    fun setup() {
        loadModules(eventBusModule)
    }

    @Test
    fun `Tile extension returns entity tile`() {
        // Given
        val entity = mockk<Entity>(relaxed = true)
        val tile = mockk<Tile>(relaxed = true)
        declareMock<Tiles> {
            every { get(entity) } returns tile
        }
        // When
        val tileResult = entity.tile()
        // Then
        assertEquals(tile, tileResult)
    }

}