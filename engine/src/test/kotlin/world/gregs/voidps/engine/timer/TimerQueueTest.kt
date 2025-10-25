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
        val list: MutableList<TimerApi> = mutableListOf(
            object : TimerApi {
                override fun start(player: Player, timer: String, restart: Boolean): Int {
                    emitted.add("start_$timer" to restart)
                    return startInterval
                }

                override fun tick(player: Player, timer: String): Int {
                    emitted.add("tick_$timer" to false)
                    return tickInterval
                }

                override fun stop(player: Player, timer: String, logout: Boolean) {
                    emitted.add("stop_$timer" to logout)
                }
            }
        )
        for (dispatcher in listOf(TimerApi.playerStartDispatcher, TimerApi.playerTickDispatcher, TimerApi.playerStopDispatcher)) {
            dispatcher.instances["timer"] = list
            dispatcher.instances["1"] = list
            dispatcher.instances["2"] = list
            dispatcher.instances["mutable"] = list
            dispatcher.instances["fixed"] = list
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
}
