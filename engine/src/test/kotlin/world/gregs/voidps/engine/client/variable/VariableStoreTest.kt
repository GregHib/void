package world.gregs.voidps.engine.client.variable

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VariableStoreTest {

    private lateinit var store: VariableStore
    private lateinit var variable: Variable<Int>

    @BeforeEach
    fun setup() {
        variable = mockk(relaxed = true)
        store = VariableStore()
    }

    @Test
    fun `Register variable`() {
        assertNull(store.get(key))
        store.register(key, variable)
        assertNotNull(store.get(key))
    }

    @Test
    fun `Clear variables`() {
        store.register(key, variable)
        assertNotNull(store.get(key))
        store.clear()
        assertNull(store.get(key))
    }

    companion object {
        private const val key = "key"
    }
}