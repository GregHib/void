package world.gregs.voidps.engine.client.variable

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VariableManagerTest {

    private lateinit var manager: VariableManager
    private lateinit var variable: Variable<Int>

    @BeforeEach
    fun setup() {
        variable = mockk(relaxed = true)
        manager = VariableManager()
    }

    @Test
    fun `Register variable`() {
        assertNull(manager.get(key))
        manager.register(key, variable)
        assertNotNull(manager.get(key))
    }

    @Test
    fun `Clear variables`() {
        manager.register(key, variable)
        assertNotNull(manager.get(key))
        manager.clear()
        assertNull(manager.get(key))
    }

    companion object {
        private const val key = "key"
    }
}