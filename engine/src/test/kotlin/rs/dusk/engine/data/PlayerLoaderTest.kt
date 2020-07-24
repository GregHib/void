package rs.dusk.engine.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
internal class PlayerLoaderTest : KoinMock() {

    override val modules = listOf(eventModule)

    @Test
    fun `load strategy`() {
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns mockk()
        val loader = PlayerLoader(strategy)
        // When
        loader.loadPlayer("test")
        // Then
        verify { strategy.load("test") }
    }

    @Test
    fun `load empty`() {
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns null
        val loader = PlayerLoader(strategy)
        // When
        val result = loader.loadPlayer("test")
        // Then
        assertEquals(-1, result.id)
        assertEquals(-1, result.index)
        assertEquals(0, result.tile.x)
        assertEquals(0, result.tile.y)
        assertEquals(0, result.tile.plane)
        verifyOrder {
            strategy.load("test")
        }
    }

    @Test
    fun `load default`() {
        setProperty("homeX", 100)
        setProperty("homeY", 100)
        setProperty("homePlane", 1)
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns null
        val loader = PlayerLoader(strategy)
        // When
        val result = loader.loadPlayer("test")
        // Then
        assertEquals(-1, result.id)
        assertEquals(-1, result.index)
        assertEquals(100, result.tile.x)
        assertEquals(100, result.tile.y)
        assertEquals(1, result.tile.plane)
        verifyOrder {
            strategy.load("test")
        }
    }
}