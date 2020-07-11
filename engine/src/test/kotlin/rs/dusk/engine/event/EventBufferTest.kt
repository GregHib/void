package rs.dusk.engine.event

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerEvent

internal class EventBufferTest {

    private lateinit var buffer: EventBuffer
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        buffer = EventBuffer(10, EventBus())
    }

    @Test
    fun `Remove clears events`() {
        // Given
        buffer.buffered[player] = mutableListOf(mockk(), mockk())
        // When
        buffer.remove(player)
        // Then
        assertFalse(buffer.buffered.contains(player))
    }

    @Test
    fun `Emit buffers event`() {
        // Given
        val event: PlayerEvent = mockk()
        every { event.player } returns player
        // When
        buffer.emitLater(event)
        // Then
        assertTrue(buffer.buffered.contains(player))
        assertEquals(1, buffer.buffered[player]!!.size)
    }

    @Test
    fun `Emit doesn't buffer event is limit is reached`() {
        // Given
        val event: PlayerEvent = mockk()
        every { event.player } returns player
        buffer.buffered[player] = (0 until 10).map { mockk<() -> Unit>() }.toMutableList()
        // When
        buffer.emitLater(event)
        // Then
        assertEquals(10, buffer.buffered[player]!!.size)
    }

}