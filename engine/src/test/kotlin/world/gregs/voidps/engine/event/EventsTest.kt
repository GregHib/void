package world.gregs.voidps.engine.event

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import kotlin.test.Test

class EventsTest {

    @Test
    fun `Exact match`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param2"), handler)

        val result = trie.search(arrayOf("param1", "param2"))

        assertEquals(listOf(handler), result)
    }

    @Test
    fun `Wildcard match`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param#"), handler)

        val result = trie.search(arrayOf("param1", "param2"))

        assertEquals(listOf(handler), result)
    }

    @Test
    fun `Default match`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("*", "*"), handler)

        val result = trie.search(arrayOf("param1", "param2"))

        assertEquals(listOf(handler), result)
    }

    @Test
    fun `Exact takes priority over wildcards`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param2"), handler)
        trie.insert(arrayOf("param1", "param#")) {}

        val result = trie.search(arrayOf("param1", "param2"))

        assertEquals(listOf(handler), result)
    }

    @Test
    fun `Exact takes priority over mixed`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param2", "param3"), handler)
        trie.insert(arrayOf("param1", "*", "param#")) {}

        val result = trie.search(arrayOf("param1", "param2", "param3"))

        assertEquals(listOf(handler), result)
    }

    @Test
    fun `Wildcards take priority over defaults`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("param1", "param#"), handler)
        trie.insert(arrayOf("param1", "*")) {}

        val result = trie.search(arrayOf("param1", "param2"))

        assertEquals(listOf(handler), result)
    }

    @Test
    fun `Fall back to default when exact and wildcards don't match`() {
        val trie = Events()
        val handler: suspend Event.(EventDispatcher) -> Unit = {}
        trie.insert(arrayOf("*", "param_4")) {}
        trie.insert(arrayOf("*", "param_#")) {}
        trie.insert(arrayOf("*", "*"), handler)

        val result = trie.search(arrayOf("param1", "param2"))

        assertEquals(listOf(handler), result)
    }

    @Test
    fun `No match`() {
        val trie = Events()
        val result = trie.search(arrayOf("param1", "param2"))

        assertNull(result)
    }

    @Test
    fun `No matching different lengths`() {
        val trie = Events()
        trie.insert(arrayOf("*", "*", "*")) {}
        trie.insert(arrayOf("*")) {}

        val result = trie.search(arrayOf("param1", "param2"))
        assertNull(result)
    }

    @Test
    fun `Clear removes all handlers`() {
        val trie = Events()
        trie.insert(arrayOf("*", "*")) {}
        trie.clear()

        val result = trie.search(arrayOf("param1", "param2"))
        assertNull(result)
    }
}