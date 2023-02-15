package world.gregs.voidps.engine.entity

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.TimerQueue
import world.gregs.voidps.engine.timer.softTimer

internal class EffectsTest {

    lateinit var player: Player
    lateinit var events: Events
    lateinit var values: Values
    lateinit var task: Timer.(Long) -> Unit
    lateinit var timer: Timer

    @BeforeEach
    fun setup() {
        GameLoop.tick = 0
        player = mockk()
        events = mockk(relaxed = true)
        values = Values()
        every { player.events } returns events
        every { player.values } returns values
        every { player.timers } returns TimerQueue(events)
        mockkStatic("world.gregs.voidps.engine.timer.TimersKt")
        every { player.softTimer(any(), any(), any(), any()) } answers {
            task = arg(4)
            timer = mockk(relaxed = true)
            timer
        }
    }

    @Test
    fun `No active effects`() {
        assertFalse(player.hasEffect("unknown_effect"))
    }

    @Test
    fun `Start and stop effect`() {
        val effect = "effect"
        player.start(effect)
        assertTrue(player.hasEffect(effect))
        player.stop(effect)
        assertFalse(player.hasEffect(effect))
        verifyOrder {
            events.emit(EffectStart(effect))
            events.emit(EffectStop(effect))
        }
    }

    @Test
    fun `Restarting quietly doesn't re-emit`() {
        val effect = "effect"
        player.start(effect)
        assertTrue(player.hasEffect(effect))
        player.start(effect, quiet = true)
        assertTrue(player.hasEffect(effect))
        verify(exactly = 1) {
            events.emit(EffectStart(effect))
        }
        verify(exactly = 0) {
            events.emit(EffectStop(effect))
        }
    }

    @Test
    fun `Starting twice will reset effect timer`() {
        val effect = "effect"
        player.start(effect)
        player.start(effect)
        assertTrue(player.hasEffect(effect))
        verifyOrder {
            events.emit(EffectStart(effect))
            events.emit(EffectStop(effect))
            events.emit(EffectStart(effect))
        }
    }

    @Test
    fun `Remove effect after delay`() {
        val effect = "effect"
        GameLoop.tick = 10
        player.start(effect, 2)
        assertTrue(player.hasEffect(effect))
        task.invoke(timer, 12)
        assertFalse(player.hasEffect(effect))
    }

    @Test
    fun `Get remaining effect time`() {
        val effect = "effect"
        GameLoop.tick = 10
        player.start(effect, 6)
        GameLoop.tick = 14
        assertEquals(2L, player.remaining(effect))
        assertEquals(4L, player.elapsed(effect))
    }

    @Test
    fun `No remaining time after stopped`() {
        val effect = "effect"
        GameLoop.tick = 10
        player.start(effect, 6)
        player.stop(effect)
        assertEquals(-1L, player.remaining(effect))
        assertEquals(-1L, player.elapsed(effect))
    }

    @Test
    fun `Delayed removal doesn't fire after stopped`() {
        val effect = "effect"
        GameLoop.tick = 10
        player.start(effect, 2)
        GameLoop.tick = 11
        player.stop(effect)
        verify {
            timer.cancel()
        }
    }

    @Test
    fun `Save effect time remaining`() {
        val effect = "effect"
        GameLoop.tick = 10
        player.start(effect, 6)
        GameLoop.tick = 12
        player.save(effect)
        assertEquals(4L, player.values!![effect])
    }

    @Test
    fun `Restart effect from time remaining`() {
        val effect = "effect"
        GameLoop.tick = 10
        player.values!!["${effect}_effect"] = 5
        player.restart(effect)
        assertEquals(5L, player.remaining(effect))
        assertEquals(0L, player.elapsed(effect))
    }

    @Test
    fun `Toggle effect`() {
        val effect = "effect"
        assertFalse(player.hasEffect(effect))
        player.toggle(effect)
        assertTrue(player.hasEffect(effect))
        player.toggle(effect)
        assertFalse(player.hasEffect(effect))
    }
}
