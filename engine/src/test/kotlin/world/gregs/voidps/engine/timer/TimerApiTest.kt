package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.test.assertEquals

class TimerApiTest {

    @Nested
    inner class TimerStartTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            timerStart(args[0]) { restart ->
                caller.call()
                assertTrue(restart)
                0
            }
        }

        override fun invoke(args: List<String>) {
            val result = TimerApi.start(Player(), "timer", true)
            assertEquals(0, result)
        }

        override val apis = listOf(TimerApi)

    }

    @Nested
    inner class TimerTickTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            timerTick(args[0]) {
                caller.call()
                Timer.CONTINUE
            }
        }

        override fun invoke(args: List<String>) {
            val result = TimerApi.tick(Player(), "timer")
            assertEquals(-2, result)
        }

        override val apis = listOf(TimerApi)

    }

    @Nested
    inner class TimerStopTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            timerStop(args[0]) { logout ->
                caller.call()
                assertTrue(logout)
            }
        }

        override fun invoke(args: List<String>) {
            TimerApi.stop(Player(), "timer", true)
        }

        override val apis = listOf(TimerApi)

    }

    @Nested
    inner class NPCTimerStartTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcTimerStart(args[0]) { restart ->
                caller.call()
                assertTrue(restart)
                0
            }
        }

        override fun invoke(args: List<String>) {
            val result = TimerApi.start(NPC("npc"), "timer", true)
            assertEquals(0, result)
        }

        override val apis = listOf(TimerApi)

    }

    @Nested
    inner class NPCTimerTickTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcTimerTick(args[0]) {
                caller.call()
                Timer.CONTINUE
            }
        }

        override fun invoke(args: List<String>) {
            val result = TimerApi.tick(NPC("npc"), "timer")
            assertEquals(-2, result)
        }

        override val apis = listOf(TimerApi)

    }

    @Nested
    inner class NPCTimerStopTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcTimerStop(args[0]) { logout ->
                caller.call()
                assertTrue(logout)
            }
        }

        override fun invoke(args: List<String>) {
            TimerApi.stop(NPC("npc"), "timer", true)
        }

        override val apis = listOf(TimerApi)

    }

    @Nested
    inner class WorldTimerStartTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            worldTimerStart(args[0]) {
                caller.call()
                0
            }
        }

        override fun invoke(args: List<String>) {
            val result = TimerApi.start("timer")
            assertEquals(0, result)
        }

        override val apis = listOf(TimerApi)

    }

    @Nested
    inner class WorldTimerTickTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            worldTimerTick(args[0]) {
                caller.call()
                Timer.CONTINUE
            }
        }

        override fun invoke(args: List<String>) {
            val result = TimerApi.tick("timer")
            assertEquals(-2, result)
        }

        override val apis = listOf(TimerApi)

    }

    @Nested
    inner class WorldTimerStopTest : ScriptTest {
        override val checks = listOf(
            listOf("timer"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            worldTimerStop(args[0]) { shutdown ->
                caller.call()
                assertTrue(shutdown)
            }
        }

        override fun invoke(args: List<String>) {
            TimerApi.stop("timer", true)
        }

        override val apis = listOf(TimerApi)

    }

}