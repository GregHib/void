package world.gregs.voidps.engine.entity.character.mode.move

import org.junit.jupiter.api.Nested
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import kotlin.test.assertEquals

class MovedTest {

    @Nested
    inner class MovedTest : ScriptTest {
        override val checks = listOf(
            listOf<String>()
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            moved { from ->
                caller.call()
                assertEquals(Tile(1, 2), from)
            }
        }

        override fun invoke(args: List<String>) {
            Moved.player(Player(), Tile(1, 2))
        }

        override val apis = listOf(Moved)

    }

    @Nested
    inner class NPCMovedTest : ScriptTest {
        override val checks = listOf(
            listOf("npc"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcMoved(args[0]) { from ->
                caller.call()
                assertEquals(Tile(1, 2), from)
            }
        }

        override fun invoke(args: List<String>) {
            Moved.npc(NPC("npc"), Tile(1, 2))
        }

        override val apis = listOf(Moved)
    }

    @Nested
    inner class EnteredTest : ScriptTest {
        override val checks = listOf(
            listOf("area"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            entered(args[0]) { area ->
                caller.call()
                assertEquals(Rectangle(Tile(1, 2), 1, 2), area)
            }
        }

        override fun invoke(args: List<String>) {
            Moved.enter(Player(), args[0], Rectangle(Tile(1, 2), 1, 2))
        }

        override val apis = listOf(Moved)
    }

    @Nested
    inner class ExitedTest : ScriptTest {
        override val checks = listOf(
            listOf("area"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            exited(args[0]) { area ->
                caller.call()
                assertEquals(Rectangle(Tile(1, 2), 1, 2), area)
            }
        }

        override fun invoke(args: List<String>) {
            Moved.exit(Player(), args[0], Rectangle(Tile(1, 2), 1, 2))
        }

        override val apis = listOf(Moved)
    }

}