package world.gregs.voidps.engine.client.variable

import org.junit.jupiter.api.Nested
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.test.assertEquals

class VariableApiTest {

    @Nested
    inner class MovedTest : ScriptTest {
        override val checks = listOf(
            listOf("var"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            variableSet(args[0]) { key, from, to ->
                caller.call()
                assertEquals("var", key)
                assertEquals("from", from)
                assertEquals("to", to)
            }
        }

        override fun invoke(args: List<String>) {
            VariableApi.set(Player(), "var", "from", "to")
        }

        override val apis = listOf(VariableApi)

    }

    @Nested
    inner class NPCMovedTest : ScriptTest {
        override val checks = listOf(
            listOf("var", "npc"),
            listOf("*", "npc"),
            listOf("var", "*"),
            listOf("*", "*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcVariableSet(args[0], args[1]) { key, from, to ->
                caller.call()
                assertEquals("var", key)
                assertEquals("from", from)
                assertEquals("to", to)
            }
        }

        override fun invoke(args: List<String>) {
            VariableApi.set(NPC("npc"), "var", "from", "to")
        }

        override val apis = listOf(VariableApi)

    }

    @Nested
    inner class VariableBitAddedTest : ScriptTest {
        override val checks = listOf(
            listOf("var"),
        )
        override val failedChecks = listOf(
            listOf("*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            variableBitAdded(args[0]) { value ->
                caller.call()
                assertEquals("val", value)
            }
        }

        override fun invoke(args: List<String>) {
            VariableApi.add(Player(), "var", "val")
        }

        override val apis = listOf(VariableApi)

    }

    @Nested
    inner class VariableBitRemovedTest : ScriptTest {
        override val checks = listOf(
            listOf("var"),
        )
        override val failedChecks = listOf(
            listOf("*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            variableBitRemoved(args[0]) { value ->
                caller.call()
                assertEquals("val", value)
            }
        }

        override fun invoke(args: List<String>) {
            VariableApi.remove(Player(), "var", "val")
        }

        override val apis = listOf(VariableApi)

    }

}