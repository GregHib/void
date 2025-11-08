package world.gregs.voidps.engine.entity.character.mode.interact

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.koin.test.mock.declare
import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.script
import world.gregs.voidps.engine.script.KoinMock

abstract class OnInteractTest : KoinMock() {

    @BeforeEach
    fun setup() {
        declare { mockk<StepValidator>(relaxed = true) }
        declare { mockk<LineValidator>(relaxed = true) }
        declare { mockk<PathFinder>(relaxed = true) }
        declare {
            val def = mockk<NPCDefinitions>(relaxed = true)
            every { def.resolve(any(), any()) } returns NPCDefinition(0, stringId = "npc")
            def
        }
        declare {
            val def = mockk<ObjectDefinitions>(relaxed = true)
            every { def.resolve(any(), any()) } returns ObjectDefinition(0, stringId = "obj")
            every { def.get(any<Int>()) } returns ObjectDefinition(0, stringId = "obj")
            def
        }
    }

    open val checks: List<List<String>> = emptyList()

    open val failedChecks: List<List<String>> = emptyList()

    abstract val operate: Script.(args: List<String>, caller: Caller) -> Unit
    abstract val approach: Script.(args: List<String>, caller: Caller) -> Unit

    abstract fun interact(): Interact

    @TestFactory
    fun `Check operate interactions`() = checks.map { args ->
        dynamicTest("Check ${args.joinToString(" on ")} operate") {
            val caller = Caller()
            script {
                operate(args, caller)
            }

            val interact = interact()
            assertTrue(interact.hasOperate())
            assertFalse(interact.hasApproach())

            assertFalse(caller.called)
            interact.operate()
            assertTrue(caller.called)
            Operation.close()
        }
    }

    @TestFactory
    fun `Check approach interactions`() = checks.map { args ->
        dynamicTest("Check ${args.joinToString(" on ")} approach") {
            val caller = Caller()
            script {
                approach(args, caller)
            }

            val interact = interact()
            assertTrue(interact.hasApproach())
            assertFalse(interact.hasOperate())

            assertFalse(caller.called)
            interact.approach()
            assertTrue(caller.called)
            Approachable.close()
        }
    }

    @TestFactory
    fun `Check non-matching operate interactions`() = failedChecks.map { args ->
        dynamicTest("Doesn't have ${args.joinToString(" on ")} operate") {
            val caller = Caller()
            script {
                operate(args, caller)
            }

            val interact = interact()
            assertFalse(interact.hasOperate())
            assertFalse(interact.hasApproach())
            Operation.close()
        }
    }

    @TestFactory
    fun `Check non-matching approach interactions`() = failedChecks.map { args ->
        dynamicTest("Doesn't have ${args.joinToString(" on ")} approach") {
            val caller = Caller()
            script {
                approach(args, caller)
            }

            val interact = interact()
            assertFalse(interact.hasApproach())
            assertFalse(interact.hasOperate())
            Approachable.close()
        }
    }

    @Test
    fun `Check operate close removes handlers`() {
        val caller = Caller()
        script {
            operate(checks.first(), caller)
        }

        val interact = interact()
        Operation.close()
        assertFalse(interact.hasOperate())
        assertFalse(interact.hasApproach())
        interact.operate()
        assertFalse(caller.called)
    }

    @Test
    fun `Check approach close removes handler`() {
        val caller = Caller()
        script {
            approach(checks.first(), caller)
        }

        val interact = interact()
        Approachable.close()
        assertFalse(interact.hasApproach())
        assertFalse(interact.hasOperate())
        interact.approach()
        assertFalse(caller.called)
    }

    @AfterEach
    fun teardown() {
        Operation.close()
        Approachable.close()
    }
}