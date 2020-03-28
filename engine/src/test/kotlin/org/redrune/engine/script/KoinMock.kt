package org.redrune.engine.script

import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.redrune.engine.script.koin.KoinTestExtension
import org.redrune.engine.script.koin.MockProviderExtension

/**
 * Class for mocking injections
 */
@ExtendWith(MockKExtension::class)
abstract class KoinMock : KoinTest {

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

    fun loadModules(vararg modules: Module) =
        koinTestExtension.koin.loadModules(modules.toList())

    fun setProperty(key: String, value: Any) =
        koinTestExtension.koin.setProperty(key, value)

}