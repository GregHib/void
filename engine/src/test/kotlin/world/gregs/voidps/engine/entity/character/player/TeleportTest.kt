package world.gregs.voidps.engine.entity.character.player

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.script.KoinMock
import kotlin.test.assertEquals

class TeleportTest {

    @Nested
    inner class TeleportTakeOffTest : ScriptTest {
        override val checks = listOf(
            listOf("tele"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            teleportTakeOff(args[0]) {
                caller.call()
                true
            }
        }

        override fun invoke(args: List<String>) {
            assertTrue(Teleport.takeOff(Player(), "tele"))
        }

        override val apis = listOf(Teleport)

    }

    @Nested
    inner class TeleportLandTest : ScriptTest {
        override val checks = listOf(
            listOf("tele"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            teleportLand(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Teleport.land(Player(), "tele")
        }

        override val apis = listOf(Teleport)

    }

    @Nested
    inner class ObjTeleportTakeOffTest : KoinMock(), ScriptTest {
        override val checks = listOf(
            listOf("option", "obj"),
            listOf("option", "*"),
            listOf("*", "obj"),
            listOf("*", "*"),
        )
        override val failedChecks = emptyList<List<String>>()

        @BeforeEach
        fun setup() {
            ObjectDefinitions.init(arrayOf(ObjectDefinition(0, stringId = "obj")))
        }

        override fun Script.register(args: List<String>, caller: Caller) {
            objTeleportTakeOff(args[0], args[1]) { obj, option ->
                caller.call()
                assertEquals(GameObject(0), obj)
                assertEquals("option", option)
                Teleport.CONTINUE
            }
        }

        override fun invoke(args: List<String>) {
            assertEquals(0, Teleport.takeOff(Player(), GameObject(0), "option"))
        }

        override val apis = listOf(Teleport)

    }


    @Nested
    inner class ObjTeleportLandTest : KoinMock(), ScriptTest {
        override val checks = listOf(
            listOf("option", "obj"),
            listOf("option", "*"),
            listOf("*", "obj"),
            listOf("*", "*"),
        )
        override val failedChecks = emptyList<List<String>>()

        @BeforeEach
        fun setup() {
            ObjectDefinitions.init(arrayOf(ObjectDefinition(0, stringId = "obj")))
        }

        override fun Script.register(args: List<String>, caller: Caller) {
            objTeleportLand(args[0], args[1]) { obj, option ->
                caller.call()
                assertEquals(GameObject(0), obj)
                assertEquals("option", option)
                Teleport.CONTINUE
            }
        }

        override fun invoke(args: List<String>) {
            Teleport.land(Player(), GameObject(0), "option")
        }

        override val apis = listOf(Teleport)

    }

}