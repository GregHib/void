import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier

/**
 * Shuts down the shared [WorldTest] world before a test class which doesn't use it,
 * so unit tests never run with world state loaded.
 */
class WorldTestListener : TestExecutionListener {
    override fun executionStarted(testIdentifier: TestIdentifier) {
        val source = testIdentifier.source.orElse(null) as? ClassSource ?: return
        var type: Class<*>? = source.javaClass
        while (type != null) {
            if (WorldTest::class.java.isAssignableFrom(type)) {
                return
            }
            type = type.enclosingClass
        }
        WorldTest.shutdown()
    }
}
