package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.SuspendableEvent
import java.util.*

abstract class TimersTest {

    lateinit var emitted: LinkedList<Event>
    lateinit var events: EventDispatcher
    lateinit var timers: Timers
    internal var block: ((Event) -> Unit)? = null

    open fun setup() {
        GameLoop.tick = 0
        emitted = LinkedList()
        events = object : EventDispatcher {
            override fun <E : Event> emit(event: E): Boolean {
                block?.invoke(event)
                emitted.add(event)
                return super.emit(event)
            }

            override fun <E : SuspendableEvent> emit(event: E): Boolean {
                block?.invoke(event)
                emitted.add(event)
                return super.emit(event)
            }
        }
        block = null
    }

    @Test
    fun `Restart a timer`() {
        block = {
            if (it is TimerStart) {
                it.interval = 2
            }
        }
        timers.restart("timer")
        assertTrue(timers.contains("timer"))
        assertEquals(TimerStart("timer", true), emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Cancelled start event doesn't add timer`() {
        block = {
            if (it is TimerStart) {
                it.cancel()
            }
        }
        assertFalse(timers.start("timer"))
        assertFalse(timers.contains("timer"))
        assertEquals(TimerStart("timer"), emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timers emit at a constant interval`() {
        block = {
            if (it is TimerStart) {
                it.interval = 2
            }
        }
        assertTrue(timers.start("timer"))
        repeat(5) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("timer"))
        assertEquals(TimerStart("timer"), emitted.pop())
        repeat(2) {
            assertEquals(TimerTick("timer"), emitted.pop())
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timer can temp modify interval`() {
        block = {
            if (it is TimerStart) {
                it.interval = 2
            } else if (it is TimerTick) {
                it.nextInterval = 1
            }
        }
        assertTrue(timers.start("timer"))
        repeat(4) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("timer"))
        assertEquals(TimerStart("timer"), emitted.pop())
        repeat(2) {
            assertEquals(TimerTick("timer").apply { nextInterval = 1 }, emitted.pop())
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timers with 0 delay repeats every tick`() {
        block = {
            if (it is TimerStart) {
                it.interval = 0
            }
        }
        timers.start("timer")
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("timer"))
        assertEquals(TimerStart("timer"), emitted.pop())
        repeat(3) {
            assertEquals(TimerTick("timer"), emitted.pop())
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timers with cancelled tick events are removed`() {
        block = {
            if (it is TimerStart) {
                it.interval = 0
            } else if (it is TimerTick) {
                it.cancel()
            }
        }
        timers.start("timer")
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertFalse(timers.contains("timer"))
        assertEquals(TimerStart("timer"), emitted.pop())
        assertEquals(TimerTick("timer"), emitted.pop())
        assertEquals(TimerStop("timer", logout = false), emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Clearing a timer cancels it`() {
        timers.start("timer")
        timers.stop("timer")
        assertEquals(TimerStart("timer"), emitted.pop())
        assertEquals(TimerStop("timer", logout = false), emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Stopping timers emit stop`() {
        timers.start("timer")
        timers.stopAll()
        assertFalse(timers.contains("timer"))
        assertEquals(TimerStart("timer"), emitted.pop())
        assertEquals(TimerStop("timer", logout = true), emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Cleared timers are cancelled`() {
        timers.start("timer")
        timers.clearAll()
        assertFalse(timers.contains("timer"))
        assertEquals(TimerStart("timer"), emitted.pop())
        assertTrue(emitted.isEmpty())
        assertTrue(emitted.isEmpty())
    }
}
