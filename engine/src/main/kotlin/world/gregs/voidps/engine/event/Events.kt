package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.*
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import kotlin.collections.HashSet
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.contains
import kotlin.collections.getOrPut
import kotlin.collections.iterator
import kotlin.collections.mutableSetOf
import kotlin.collections.set

/**
 * Events is a Trie used for efficient storage and retrieval of handlers based on an arbitrary list of parameters.
 * Handlers are looked up by matching the number of search parameters with stored parameters.
 * A parameter matches on one of three conditions:
 * - Exact match; the values are equal
 * - Wildcard match; All characters in two strings match except the "*" wildcard which matches
 *   any characters, and the "#" wildcard which matches a single digit 0-9
 * - Default match; Always matches every input
 */
class Events(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) {
    private val roots: MutableMap<Int, TrieNode> = Int2ObjectOpenHashMap(8)
    var all: ((Player, Event) -> Unit)? = null
    private val logger = InlineLogger()

    private class TrieNode {
        val children: MutableMap<Any?, TrieNode> = Object2ObjectOpenHashMap(1, 0.5f)
        var handler: MutableSet<suspend Event.(EventDispatcher) -> Unit>? = null
    }

    /**
     * Inserts a handler into the trie based on the provided parameters. If a node for a parameter
     * does not exist, it creates a new node. The handler is added to a list within the leaf node.
     *
     * @param parameters An array of values representing the parameters associated with the handler.
     * @param handler The handler function to be associated with the provided parameters.
     */
    fun insert(parameters: Array<out Any?>, handler: suspend Event.(EventDispatcher) -> Unit) {
        var node = roots.getOrPut(parameters.size) { TrieNode() }
        for (param in parameters) {
            if (!node.children.containsKey(param)) {
                node.children[param] = TrieNode()
            }
            node = node.children[param]!!
        }
        if (node.handler == null) {
            node.handler = HashSet(1)
        }
        node.handler!!.add(handler)
    }

    /**
     * Executes all handlers with [dispatcher] and [event] as arguments
     * @return any handlers were found and executed
     */
    fun emit(dispatcher: EventDispatcher, event: Event): Boolean {
        val handlers = search(dispatcher, event) ?: return false
        runBlocking {
            handleEvent(handlers, event, dispatcher)
        }
        return true
    }

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(errorHandler, block = block)
    }

    /**
     * Executes all handlers in a coroutine with [dispatcher] and [event] as arguments
     * @return any handlers were found and executed
     */
    fun emit(dispatcher: EventDispatcher, event: SuspendableEvent): Boolean {
        val handlers = search(dispatcher, event) ?: return false
        launch {
            handleEvent(handlers, event, dispatcher)
        }
        return true
    }

    private suspend fun handleEvent(handlers: Set<suspend Event.(EventDispatcher) -> Unit>, event: Event, dispatcher: EventDispatcher) {
        for (handler in handlers) {
            if (event is CancellableEvent && event.cancelled) {
                break
            }
            try {
                handler.invoke(event, dispatcher)
            } catch (e: Exception) {
                logger.warn(e) { "Error in event handler $dispatcher $event $handler" }
            }
        }
    }

    fun contains(dispatcher: EventDispatcher, event: Event): Boolean {
        if (event.size <= 0) {
            return false
        }
        val root = roots[event.size] ?: return false
        return first(dispatcher, event, root, 0, null) != null
    }

    /**
     * Searches for a handler based on the [event] parameters. It traverses the trie based on the
     * parameters, considering exact, wildcard, and default matches. Returns the
     * handlers associated with the matching parameter combination, or null if no match is found.
     *
     * @param event An event which can look up values representing the parameters to search for.
     * @return The handler functions associated with the matching parameter combination, or null if
     * no match is found.
     */
    internal fun search(dispatcher: EventDispatcher, event: Event, skip: (suspend Event.(EventDispatcher) -> Unit)? = null): Set<suspend Event.(EventDispatcher) -> Unit>? {
        if (event.size <= 0) {
            return null
        }
        val root = roots[event.size] ?: return null
        return if (event.notification) all(dispatcher, event, root, 0, skip) else first(dispatcher, event, root, 0, skip)
    }

    private fun first(dispatcher: EventDispatcher, event: Event, node: TrieNode, depth: Int, skip: (suspend Event.(EventDispatcher) -> Unit)? = null): Set<suspend Event.(EventDispatcher) -> Unit>? {
        if (depth == event.size) {
            if (node.handler!!.contains(skip)) {
                return null
            }
            return node.handler
        }
        val param = event.parameter(dispatcher, depth)
        val exact = node.children[param]
        if (exact != null) {
            val result = first(dispatcher, event, exact, depth + 1, skip)
            if (result != null) {
                return result
            }
        }
        for ((key, child) in node.children) {
            if (key == "*" || key == param) {
                continue
            }
            if (matches(key, param)) {
                val result = first(dispatcher, event, child, depth + 1, skip)
                if (result != null) {
                    return result
                }
            }
        }
        val default = node.children["*"]
        if (default != null) {
            val result = first(dispatcher, event, default, depth + 1, skip)
            if (result != null) {
                return result
            }
        }
        return null
    }

    private fun all(
        dispatcher: EventDispatcher,
        event: Event,
        node: TrieNode,
        depth: Int,
        skip: (suspend Event.(EventDispatcher) -> Unit)? = null,
        output: MutableSet<suspend Event.(EventDispatcher) -> Unit> = mutableSetOf(),
    ): Set<suspend Event.(EventDispatcher) -> Unit> {
        if (depth == event.size) {
            if (node.handler!!.contains(skip)) {
                return output
            }
            output.addAll(node.handler ?: return output)
            return output
        }
        val param = event.parameter(dispatcher, depth)
        for ((key, child) in node.children) {
            if (key == "*") {
                continue
            }
            if (matches(key, param)) {
                all(dispatcher, event, child, depth + 1, skip, output)
            }
        }
        val default = node.children["*"]
        if (default != null) {
            all(dispatcher, event, default, depth + 1, skip, output)
        }
        return output
    }

    private fun matches(key: Any?, param: Any?): Boolean = when {
        key is String && param is String -> wildcardEquals(key, param)
        param is Set<*> -> param.contains(key)
        key is Set<*> -> key.contains(param)
        key is Area -> param is Tile && key.contains(param)
        else -> key == param
    }

    fun clear() {
        roots.clear()
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        private val logger = InlineLogger()
        private val errorHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                logger.warn(throwable) { "Error in event." }
            }
        }
        var events = Events()
            private set

        fun setEvents(events: Events) {
            this.events = events
        }

        @JvmName("handleDispatcher")
        fun <D : EventDispatcher, E : Event> handle(vararg parameters: Any?, handler: suspend E.(D) -> Unit) {
            handle(parameters, handler as suspend Event.(EventDispatcher) -> Unit)
        }

        @JvmName("handleEventDispatcher")
        fun <D : EventDispatcher, E : Event> handle(parameters: Array<out Any?>, handler: suspend E.(D) -> Unit) {
            handle(parameters, handler as suspend Event.(EventDispatcher) -> Unit)
        }

        @JvmName("handleEvent")
        fun <E : Event> handle(vararg parameters: Any?, handler: suspend E.(EventDispatcher) -> Unit) {
            handle(parameters, handler as suspend Event.(EventDispatcher) -> Unit)
        }

        private fun handle(parameters: Array<out Any?>, handler: suspend Event.(EventDispatcher) -> Unit) {
            events.insert(parameters, handler)
        }
    }
}

@JvmName("onEventDispatcher")
inline fun <D : EventDispatcher, reified E : Event> onEvent(vararg parameters: Any = arrayOf(E::class.simpleName!!.toSnakeCase()), noinline handler: suspend E.(D) -> Unit) {
    Events.handle(parameters, handler)
}

@JvmName("onEvent")
inline fun <reified E : Event> onEvent(vararg parameters: Any = arrayOf(E::class.simpleName!!.toSnakeCase()), noinline handler: suspend E.(EventDispatcher) -> Unit) {
    Events.handle(parameters, handler)
}
