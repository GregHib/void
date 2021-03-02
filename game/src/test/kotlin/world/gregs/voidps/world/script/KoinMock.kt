package world.gregs.voidps.world.script

import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.Module
import org.koin.test.KoinTest
import world.gregs.voidps.world.script.koin.KoinTestExtension
import world.gregs.voidps.world.script.koin.MockProviderExtension

/**
 * Class for mocking injections
 */
@ExtendWith(MockKExtension::class)
abstract class KoinMock : KoinTest {

    open val modules: List<Module>? = null
    open val properties: List<Pair<String, String>>? = null
    open val propertyPaths: List<String>? = null

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        printLogger()
        if (modules != null) {
            modules(modules!!)
        }
        propertyPaths?.forEach { path ->
            fileProperties(path)
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

    fun setProperty(key: String, value: String) =
        koinTestExtension.koin.setProperty(key, value)

    fun setProperty(key: String, value: Int) =
        koinTestExtension.koin.setProperty(key, value.toString())

    fun setProperty(key: String, value: Float) =
        koinTestExtension.koin.setProperty(key, value.toString())

}