package world.gregs.voidps.engine

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

interface ScriptTest {

    val apis: List<AutoCloseable>

    val checks: List<List<String>>
    val failedChecks: List<List<String>>

    fun Script.register(args: List<String>, caller: Caller)

    fun invoke(args: List<String>)

    @TestFactory
    fun `Check scripts`() = checks.map { args ->
        dynamicTest("Check ${args.joinToString(" ")} script") {
            val caller = Caller()
            script { register(args, caller) }

            assertFalse(caller.called)
            invoke(args)
            assertTrue(caller.called)
            teardown()
        }
    }

    @TestFactory
    fun `Check failed scripts`() = failedChecks.map { args ->
        dynamicTest("Check ${args.joinToString(" ")} script") {
            val caller = Caller()
            script { register(args, caller) }
            invoke(args)
            assertFalse(caller.called)
            teardown()
        }
    }

    @Test
    fun `Check close removes handlers`() {
        val caller = Caller()
        val args = checks.firstOrNull() ?: emptyList()
        script { register(args, caller) }

        teardown()
        invoke(args)
        assertFalse(caller.called)
    }

    @AfterEach
    fun teardown() {
        for (api in apis) {
            api.close()
        }
    }
}

class Caller(var called: Boolean = false) {
    fun call() {
        called = true
    }
}

fun script(block: Script.() -> Unit) {
    object : Script {
        init {
            block(this)
        }
    }
}