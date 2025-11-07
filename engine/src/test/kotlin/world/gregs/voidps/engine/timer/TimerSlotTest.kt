package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.npc.NPC

internal class TimerSlotTest : TimersTest() {

    private var npc = NPC()

    @BeforeEach
    override fun setup() {
        super.setup()
        for (timer in listOf("timer", "1", "2")) {
            object : TimerApi {
                init {
                    npcTimerStart(timer) { restart ->
                        emitted.add("start_$timer" to restart)
                        startInterval
                    }
                    npcTimerTick(timer) {
                        emitted.add("tick_$timer" to false)
                        tickInterval
                    }
                    npcTimerStop(timer) { logout ->
                        emitted.add("stop_$timer" to logout)
                    }
                }
            }
        }
        timers = TimerSlot(npc)
    }

    @Test
    fun `Overriding cancels previous timer`() {
        timers.start("1")
        timers.start("2")
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertFalse(timers.contains("1"))
        assertTrue(timers.contains("2"))
        assertEquals("start_1" to false, emitted.pop())
        assertEquals("start_2" to false, emitted.pop())
        assertEquals("stop_1" to false, emitted.pop())
        assertEquals("tick_2" to false, emitted.pop())
        assertEquals("tick_2" to false, emitted.pop())
        assertEquals("tick_2" to false, emitted.pop())
        assertTrue(emitted.isEmpty())
    }
}
