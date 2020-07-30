package rs.dusk.world.script

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.script.templates.standard.ScriptTemplateWithArgs

/**
 * Class for testing scripts by mocking injections
 */
@ExtendWith(MockKExtension::class)
abstract class ScriptMock(private val suffix: String = "Test") : KoinMock() {

    protected lateinit var script: ScriptTemplateWithArgs

    private fun loadScriptClass(): Class<*> {
        val clazz = this::class.java
        val scriptPackage = "${clazz.packageName}.${clazz.simpleName.substring(0, clazz.simpleName.length - suffix.length)}"
        return Class.forName(scriptPackage)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> loadScript(): T {
        val clazz = this::class.java
        val scriptPackage = "${clazz.packageName}.${clazz.simpleName.substring(0, clazz.simpleName.length - suffix.length)}"
        return Class.forName(scriptPackage).constructors.first().newInstance(emptyArray<String>()) as T
    }

    @BeforeEach
    open fun setup() {
        script = loadScript()
    }

}