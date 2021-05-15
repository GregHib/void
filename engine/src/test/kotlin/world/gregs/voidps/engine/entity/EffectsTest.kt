package world.gregs.voidps.engine.entity

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Job
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.event.Events

internal class EffectsTest {

    lateinit var entity: Entity
    lateinit var events: Events
    lateinit var values: Values
    lateinit var task: (Long) -> Unit
    lateinit var job: Job

    @BeforeEach
    fun setup() {
        entity = mockk()
        events = mockk(relaxed = true)
        values = Values()
        every { entity.events } returns events
        every { entity.values } returns values
        mockkStatic("world.gregs.voidps.engine.GameLoopKt")
        every { delay(any(), any(), any()) } answers {
            task = arg(2)
            job = mockk(relaxed = true)
            job
        }
    }

    @Test
    fun `No active effects`() {
        assertFalse(entity.has("unknown_effect"))
    }

    @Test
    fun `Start and stop effect`() {
        val effect = "effect"
        entity.start(effect)
        assertTrue(entity.has(effect))
        entity.stop(effect)
        assertFalse(entity.has(effect))
    }

    @Test
    fun `Remove effect after delay`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.start(effect, 2)
        assertTrue(entity.has(effect))
        task.invoke(12)
        assertFalse(entity.has(effect))
    }

    @Test
    fun `Get remaining effect time`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.start(effect, 6)
        GameLoop.tick = 14
        assertEquals(2, entity.remaining(effect))
    }

    @Test
    fun `No remaining time after stopped`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.start(effect, 6)
        entity.stop(effect)
        assertEquals(-1, entity.remaining(effect))
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
        assertEquals(4, entity.values[effect])
    }

    @Test
    fun `Restart effect from time remaining`() {
        val effect = "effect"
        GameLoop.tick = 10
        entity.values[effect] = 5
        entity.restart(effect)
        assertEquals(5, entity.remaining(effect))
    }

    @Test
    fun `Toggle effect`() {
        val effect = "effect"
        assertFalse(entity.has(effect))
        entity.toggle(effect)
        assertTrue(entity.has(effect))
        entity.toggle(effect)
        assertFalse(entity.has(effect))
    }
}