import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.fileProperties
import org.koin.test.KoinTest

/**
 * Class for mocking injections
 */
@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class KoinMock : KoinTest {

    open val modules: List<Module>? = null
    open val properties: List<Pair<String, String>>? = null
    open val propertyPaths: List<String>? = null

    @BeforeAll
    fun beforeAll() {
        startKoin {
            printLogger(Level.ERROR)
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
    }

    @AfterAll
    fun afterAll() {
        stopKoin()
    }

    @JvmField
    @RegisterExtension
    val mockProvider = MockProviderExtension.create { clazz ->
        mockkClass(clazz)
    }

    fun loadModules(vararg modules: Module) = getKoin().loadModules(modules.toList())

    fun setProperty(key: String, value: String) = getKoin().setProperty(key, value)

    fun setProperty(key: String, value: Int) = getKoin().setProperty(key, value.toString())

    fun setProperty(key: String, value: Float) = getKoin().setProperty(key, value.toString())
}
