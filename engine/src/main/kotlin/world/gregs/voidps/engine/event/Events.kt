package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.CoroutineContext

/**
 * Events is a Trie used for efficient storage and retrieval of handlers based on an arbitrary list of parameters.
 * Handlers are looked up by matching the number of search parameters with stored parameters.
 * A parameter matches on one of three conditions:
 * - Exact match; the strings are identical
 * - Wildcard match; All characters in the string match except the "*" wildcard which matches
 *   any characters, and the "#" wildcard which matches a single digit 0-9
 * - Default match; Always matches every input
 */
class Events : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined + errorHandler
    private val roots = mutableMapOf<Int, TrieNode>()
    var all: ((Player, Event) -> Unit)? = null

    private class TrieNode {
        val children: MutableMap<String, TrieNode> = Object2ObjectOpenHashMap()
        var handler: MutableList<suspend Event.(EventDispatcher) -> Unit>? = null
    }

    /**
     * Inserts a handler into the trie based on the provided parameters. If a node for a parameter
     * does not exist, it creates a new node. The handler is added to a list within the leaf node.
     *
     * @param parameters An array of strings representing the parameters associated with the handler.
     * @param handler The handler function to be associated with the provided parameters.
     */
    fun insert(parameters: Array<out String>, handler: suspend Event.(EventDispatcher) -> Unit) {
        var node = roots.getOrPut(parameters.size) { TrieNode() }
        for (param in parameters) {
            if (!node.children.containsKey(param)) {
                node.children[param] = TrieNode()
            }
            node = node.children[param]!!
        }
        if (node.handler == null) {
            node.handler = mutableListOf()
        }
        node.handler!!.add(handler)
    }

    /**
     * Executes all handlers with [dispatcher] and [event] as arguments
     * @return any handlers were found and executed
     */
    fun emit(dispatcher: EventDispatcher, event: Event): Boolean {
        val handlers = search(dispatcher, event) ?: return false
        if (dispatcher is Player && dispatcher.contains("bot")) {
            all?.invoke(dispatcher, event)
        }
        runBlocking {
            for (handler in handlers) {
                if (event is CancellableEvent && event.cancelled) {
                    break
                }
                handler.invoke(event, dispatcher)
            }
        }
        return true
    }

    /**
     * Executes all handlers in a coroutine with [dispatcher] and [event] as arguments
     * @return any handlers were found and executed
     */
    fun emit(dispatcher: EventDispatcher, event: SuspendableEvent): Boolean {
        val handlers = search(dispatcher, event) ?: return false
        if (dispatcher is Player && dispatcher.contains("bot")) {
            all?.invoke(dispatcher, event)
        }
        launch {
            for (handler in handlers) {
                if (event is CancellableEvent && event.cancelled) {
                    break
                }
                handler.invoke(event, dispatcher)
            }
        }
        return true
    }

    fun contains(dispatcher: EventDispatcher, event: Event): Boolean {
        val root = roots[event.size()] ?: return false
        return search(dispatcher, event, root, 0, BooleanArray(event.size()) { false }) != null
    }

    /**
     * Searches for a handler based on the [event] parameters. It traverses the trie based on the
     * parameters, considering exact, wildcard, and default matches. Returns the
     * handlers associated with the matching parameter combination, or null if no match is found.
     *
     * @param event An event which can look up strings representing the parameters to search for.
     * @return The handler functions associated with the matching parameter combination, or null if
     * no match is found.
     */
    internal fun search(dispatcher: EventDispatcher, event: Event, skipExact: BooleanArray = BooleanArray(event.size()) { false }): List<suspend Event.(EventDispatcher) -> Unit>? {
        val root = roots[event.size()] ?: return null
        return search(dispatcher, event, root, 0, skipExact)
    }

    private fun search(dispatcher: EventDispatcher, event: Event, node: TrieNode, depth: Int, skipExact: BooleanArray): List<suspend Event.(EventDispatcher) -> Unit>? {
        if (depth == event.size()) {
            return node.handler
        }
        val param = event.parameter(dispatcher, depth)
        if (!skipExact[depth]) {
            val exact = node.children[param]
            if (exact != null) {
                val result = search(dispatcher, event, exact, depth + 1, skipExact)
                if (result != null) {
                    return result
                }
            }
        }
        for ((key, child) in node.children) {
            if (key == "*" || key == param) {
                continue
            }
            if (wildcardEquals(key, param)) {
                val result = search(dispatcher, event, child, depth + 1, skipExact)
                if (result != null) {
                    return result
                }
            }
        }
        val default = node.children["*"]
        if (default != null) {
            return search(dispatcher, event, default, depth + 1, skipExact)
        }
        return null
    }

    fun clear() {
        roots.clear()
    }

    companion object {
        private val logger = InlineLogger()
        private val errorHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                logger.warn(throwable) { "Error in event." }
            }
        }
        var handlers = Events()
            private set

        fun setHandlers(handlers: Events) {
            this.handlers = handlers
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : EventDispatcher, E : Event> handle(vararg parameters: String, skipDefault: BooleanArray? = null, block: suspend E.(T) -> Unit) {
            val handler = block as suspend Event.(EventDispatcher) -> Unit
            if (skipDefault != null) {
                check(skipDefault.size == parameters.size) { "Skip default array must be the same size as parameters: ${parameters.size}." }
                handlers.insert(parameters) { entity ->
                    handler.invoke(this, entity)
                    if (this is CancellableEvent && this.cancelled) {
                        return@insert
                    }
                    val handlers = Events.handlers.search(entity, this, skipDefault) ?: return@insert
                    for (h in handlers) {
                        if (entity is CancellableEvent && entity.cancelled) {
                            break
                        }
                        h.invoke(this, entity)
                    }
                }
            } else {
                handlers.insert(parameters, handler)
            }
        }
    }
}