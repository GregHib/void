package world.gregs.voidps.engine.entity

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.tick.Job
import world.gregs.voidps.engine.tick.delay

internal class EffectsTest {

    lateinit var entity: Entity
    lateinit var events: Events
    lateinit var values: Values
    lateinit var task: Job.(Long) -> Unit
    lateinit var job: Job

    @BeforeEach
    fun setup() {
        entity = mockk()
        events = mockk(relaxed = true)
        values = Values()
        every { entity.events } returns events
        every { entity.values } returns values
        mockkStatic("world.gregs.voidps.engine.tick.SchedulerKt")
        every { delay(any(), any(), any()) } answers {
            task = arg(2)
            job = mockk(relaxed = true)
            job
        }
    }

    @Test
    fun `No active effects`() {
        assertFalse(entity.hasEffect("unknown_effect"))
    }

    @Test
    fun `Start and stop effect`() {
        val effect = "effect"
        entity.start(effect)
        assertTrue(entity.hasEffect(effect))
        entity.stop(effect)
        assertFalse(entity.hasEffect(effect))
        verifyOrder {
            events.emit(EffectStart(effect))
            events.emit(EffectStop(effect))
        }
    }

    @Test
    fun `Restarting quietly doesn't re-emit`() {
        val effect = "effect"
        entity.start(effect)
        assertTrue(entity.hasEffect(effect))
        entity.start(effect, quiet = true)
        assertTrue(entity.hasEffect(effect))
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
        entity.start(effect)
        entity.start(effect)
        assertTrue(entity.hasEffect(effect))
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
        entity.start(effect, 2)
        assertTrue(entity.hasEffect(effect))
        task.invoke(job, 12)
        assertFalse(entity.hasEffect(effect))
    }

    @Test
    fun `Get remaining effect time`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.start(effect, 6)
        GameLoop.tick = 14
        assertEquals(2L, entity.remaining(effect))
        assertEquals(4L, entity.elapsed(effect))
    }

    @Test
    fun `No remaining time after stopped`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.start(effect, 6)
        entity.stop(effect)
        assertEquals(-1L, entity.remaining(effect))
        assertEquals(-1L, entity.elapsed(effect))
    }

    @Test
    fun `Delayed removal doesn't fire after stopped`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.start(effect, 2)
        GameLoop.tick = 11
        entity.stop(effect)
        verify {
            job.cancel()
        }
    }

    @Test
    fun `Save effect time remaining`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.start(effect, 6)
        GameLoop.tick = 12
        entity.save(effect)
        assertEquals(4L, entity.values[effect])
    }

    @Test
    fun `Restart effect from time remaining`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.values["${effect}_effect"] = 5
        entity.restart(effect)
        assertEquals(5L, entity.remaining(effect))
        assertEquals(0L, entity.elapsed(effect))
    }

    @Test
    fun `Toggle effect`() {
        val effect = "effect"
        assertFalse(entity.hasEffect(effect))
        entity.toggle(effect)
        assertTrue(entity.hasEffect(effect))
        entity.toggle(effect)
        assertFalse(entity.hasEffect(effect))
    }
}