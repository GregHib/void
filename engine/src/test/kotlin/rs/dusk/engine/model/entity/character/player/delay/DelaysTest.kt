package rs.dusk.engine.model.entity.character.player.delay

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import rs.dusk.engine.GameLoop

internal class DelaysTest {

    @Test
    fun `Is delayed before`() {
        // Given
        val delays = Delays()
        GameLoop.tick = 4
        delays.start(Delay.DoorSlam)
        // When
        GameLoop.tick = GameLoop.tick + Delay.DoorSlam.ticks - 1
        val delayed = delays.isDelayed(Delay.DoorSlam)
        // Then
        assertTrue(delayed)
    }

    @Test
    fun `Is delayed equals to delay`() {
        // Given
        val delays = Delays()
        GameLoop.tick = 4
        delays.start(Delay.DoorSlam)
        GameLoop.tick += Delay.DoorSlam.ticks
        // When
        val delayed = delays.isDelayed(Delay.DoorSlam)
        // Then
        assertTrue(delayed)
    }

    @Test
    fun `Isn't delayed after`() {
        // Given
        val delays = Delays()
        GameLoop.tick = 4
        delays.start(Delay.DoorSlam)
        // When
        GameLoop.tick = GameLoop.tick + Delay.DoorSlam.ticks + 1
        val delayed = delays.isDelayed(Delay.DoorSlam)
        // Then
        assertFalse(delayed)
    }

    @Test
    fun `Elapsed after time`() {
        // Given
        val delays = Delays()
        GameLoop.tick = 4
        delays.start(Delay.DoorSlam)
        GameLoop.tick += 5
        // When
        val elapsed = delays.elapsed(Delay.DoorSlam)
        // Then
        assertEquals(5, elapsed)
    }

    @Test
    fun `Elapsed defaults to zero`() {
        // Given
        val delays = Delays()
        GameLoop.tick = 11
        // When
        val elapsed = delays.elapsed(Delay.DoorSlam)
        // Then
        assertEquals(0, elapsed)
    }

    @Test
    fun `Delayed starts if clear`() {
        // Given
        val delays = Delays()
        // When
        val initial = delays.delayed(Delay.DoorSlam)
        val delayed = delays.isDelayed(Delay.DoorSlam)
        // Then
        assertFalse(initial)
        assertTrue(delayed)
    }

    @Test
    fun `Start sets tick`() {
        // Given
        val delays = Delays()
        GameLoop.tick = 42
        // When
        delays.start(Delay.DoorSlam)
        val elapsed = delays.elapsed(Delay.DoorSlam)
        // Then
        assertEquals(0, elapsed)
    }

    @Test
    fun `Clear resets`() {
        // Given
        val delays = Delays()
        GameLoop.tick = 11
        delays.start(Delay.DoorSlam)
        GameLoop.tick = 12
        // When
        delays.reset(Delay.DoorSlam)
        // Then
        assertEquals(0, delays.elapsed(Delay.DoorSlam))
    }
}