package rs.dusk.engine.script

import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.Module
import org.koin.test.KoinTest
import rs.dusk.engine.script.koin.KoinTestExtension
import rs.dusk.engine.script.koin.MockProviderExtension

/**
 * Class for mocking injections
 */
@ExtendWith(MockKExtension::class)
abstract class KoinMock : KoinTest {

    open val modules: List<Module>? = null
    open val properties: List<Pair<String, Any>>? = null

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        printLogger()
        if (modules != null) {
            modules(modules!!)
        }
        if (properties != null) {
            properties(properties!!.toMap())
        }
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