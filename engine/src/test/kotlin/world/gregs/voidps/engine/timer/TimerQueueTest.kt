package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.player.Player

internal class TimerQueueTest : TimersTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        timers = TimerQueue(Player())
        for (timer in listOf("timer", "1", "2", "mutable", "fixed")) {
            object : TimerApi {
                init {
                    timerStart(timer) { restart ->
                        emitted.add("start_$timer" to restart)
                        startInterval
                    }
                    timerTick(timer) {
                        emitted.add("tick_$timer" to false)
                        tickInterval
                    }
                    timerStop(timer) { logout ->
                        emitted.add("stop_$timer" to logout)
                    }
                }
            }
        }
    }

    @Test
    fun `Multiple timers run at once`() {
        timers.start("1")
        timers.start("2")
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("1"))
        assertTrue(timers.contains("2"))
        assertEquals("start_1", emitted.pop().first)
        assertEquals("start_2", emitted.pop().first)
        repeat(3) {
            assertEquals("tick_1", emitted.pop().first)
            assertEquals("tick_2", emitted.pop().first)
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Updating next timer tick changes order`() {
        startInterval = 2
        timers.start("mutable")
        startInterval = 3
        timers.start("fixed")

        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertFalse(timers.contains("timer"))
        assertEquals("start_mutable", emitted.pop().first)
        assertEquals("start_fixed", emitted.pop().first)
        assertEquals("tick_mutable", emitted.pop().first)
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Can't run two timers with the same name`() {
        assertTrue(timers.start("1"))
        assertFalse(timers.start("1"))
    }

    @Test
    fun `Timer stopped during its own tick isn't requeued`() {
        object : TimerApi {
            init {
                timerStart("self_stop") { 0 }
                timerTick("self_stop") {
                    this@TimerQueueTest.timers.stop("self_stop")
                    Timer.CONTINUE
                }
                timerStop("self_stop") { logout ->
                    emitted.add("stop_self_stop" to logout)
                }
            }
        }
        timers.start("self_stop")
        timers.run()
        assertFalse(timers.contains("self_stop"))
        assertTrue((timers as TimerQueue).queue.isEmpty())
        assertEquals("stop_self_stop", emitted.pop().first)
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timer restarted during its own tick isn't duplicated`() {
        object : TimerApi {
            init {
                timerStart("restart") { 1 }
                timerTick("restart") {
                    this@TimerQueueTest.timers.stop("restart")
                    this@TimerQueueTest.timers.start("restart")
                    Timer.CONTINUE
                }
            }
        }
        timers.start("restart")
        GameLoop.tick++
        timers.run()
        assertTrue(timers.contains("restart"))
        assertEquals(1, (timers as TimerQueue).queue.count { it.name == "restart" })
    }

    @Test
    fun `Timers cleared during a tick aren't requeued`() {
        object : TimerApi {
            init {
                timerStart("clear_all") { 0 }
                timerTick("clear_all") {
                    this@TimerQueueTest.timers.clearAll()
                    Timer.CONTINUE
                }
            }
        }
        timers.start("clear_all")
        timers.run()
        assertFalse(timers.contains("clear_all"))
        assertTrue((timers as TimerQueue).queue.isEmpty())
    }
}
