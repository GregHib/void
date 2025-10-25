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
        val list: MutableList<TimerApi> = mutableListOf(
            object : TimerApi {
                override fun start(npc: NPC, timer: String, restart: Boolean): Int {
                    emitted.add("start_$timer" to restart)
                    return startInterval
                }

                override fun tick(npc: NPC, timer: String): Int {
                    emitted.add("tick_$timer" to false)
                    return tickInterval
                }

                override fun stop(npc: NPC, timer: String, death: Boolean) {
                    emitted.add("stop_$timer" to death)
                }
            }
        )
        for (dispatcher in listOf(TimerApi.npcStartDispatcher, TimerApi.npcTickDispatcher, TimerApi.npcStopDispatcher)) {
            dispatcher.instances["timer"] = list
            dispatcher.instances["1"] = list
            dispatcher.instances["2"] = list
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
