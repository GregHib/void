package org.redrune.engine.script

import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.redrune.engine.script.koin.KoinTestExtension
import org.redrune.engine.script.koin.MockProviderExtension
import kotlin.script.templates.standard.ScriptTemplateWithArgs

/**
 * Class for testing scripts by mocking injections
 */
@ExtendWith(MockKExtension::class)
abstract class ScriptMock : TestScriptLoader("Test"), KoinTest {

    protected lateinit var script: ScriptTemplateWithArgs
    private val modules = mutableListOf<Module>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        printLogger()
        modules(modules)
    }

    @JvmField
    @RegisterExtension
    val mockProvider = MockProviderExtension.create { clazz ->
        mockkClass(clazz)
    }

    @BeforeEach
    open fun setup() {
        script = loadScript()
    }

    fun loadModules(vararg modules: Module) =
        koinTestExtension.koin.loadModules(modules.toList())
}