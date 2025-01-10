package world.gregs.voidps.engine.event

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertTrue

class EventsTest {

    private val entity = object : EventDispatcher {}

    @Test
    fun `Exact match`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param2"), handler)

        val result = trie.search(entity, event("param1", "param2"))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Exceptions thrown in handlers are handled`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {
            throw IllegalStateException("tantrum")
        }
        trie.insert(arrayOf("param1"), handler)

        val result = trie.emit(entity, event("param1"))

        assertTrue(result)
    }

    @Test
    fun `Exact match non string values`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", 2), handler)

        val result = trie.search(entity, event("param1", 2))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Exact match null values`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", null), handler)

        val result = trie.search(entity, event("param1", null))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Wildcard match`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param#"), handler)

        val result = trie.search(entity, event("param1", "param2"))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Default match`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("*", "*"), handler)

        val result = trie.search(entity, event("param1", "param2"))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Match multiple layers when find all`() {
        val trie = Events()
        val handler1: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("*", "*"), handler1)
        val handler2: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "*"), handler2)
        val handler3: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param2"), handler3)

        val result = trie.search(entity, event("param1", "param2", findAll = true))

        assertEquals(setOf(handler1, handler2, handler3), result)
    }

    @Test
    fun `Exact takes priority over wildcards`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param2"), handler)
        trie.insert(arrayOf("param1", "param#")) {}

        val result = trie.search(entity, event("param1", "param2"))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Exact takes priority over mixed`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param2", "param3"), handler)
        trie.insert(arrayOf("param1", "*", "param#")) {}

        val result = trie.search(entity, event("param1", "param2", "param3"))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Wildcards take priority over defaults`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param#"), handler)
        trie.insert(arrayOf("param1", "*")) {}

        val result = trie.search(entity, event("param1", "param2"))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Fall back to default when exact and wildcards don't match`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("*", "param_4")) {}
        trie.insert(arrayOf("*", "param_#")) {}
        trie.insert(arrayOf("*", "*"), handler)

        val result = trie.search(entity, event("param1", "param2"))

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `Match ignoring exact`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        val exactHandler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("*", "param2"), handler)
        trie.insert(arrayOf("param1", "param2"), exactHandler)

        val result = trie.search(entity, event("param1", "param2"), skip = exactHandler)

        assertEquals(setOf(handler), result)
    }

    @Test
    fun `No match`() {
        val trie = Events()
        val result = trie.search(entity, event("param1", "param2"))

        assertNull(result)
    }

    @Test
    fun `No matching different lengths`() {
        val trie = Events()
        trie.insert(arrayOf("*", "*", "*")) {}
        trie.insert(arrayOf("*")) {}

        val result = trie.search(entity, event("param1", "param2"))
        assertNull(result)
    }

    @Test
    fun `Clear removes all handlers`() {
        val trie = Events()
        trie.insert(arrayOf("*", "*")) {}
        trie.clear()

        val result = trie.search(entity, event("param1", "param2"))
        assertNull(result)
    }

    private fun event(vararg params: Any?, findAll: Boolean = false) = object : Event {
        override val notification = findAll

        override val size = params.size

        override fun parameter(dispatcher: EventDispatcher, index: Int) = params[index]
    }
}