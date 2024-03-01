package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.character.CharacterContext
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
        var handler: MutableSet<suspend Event.(EventDispatcher) -> Unit>? = null
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
            node.handler = mutableSetOf()
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
        return search(dispatcher, event, root, 0, null) != null
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
    internal fun search(dispatcher: EventDispatcher, event: Event, skip: (suspend Event.(EventDispatcher) -> Unit)? = null): Set<suspend Event.(EventDispatcher) -> Unit>? {
        val root = roots[event.size()] ?: return null
        return search(dispatcher, event, root, 0, skip)
    }

    private fun search(dispatcher: EventDispatcher, event: Event, node: TrieNode, depth: Int, skip: (suspend Event.(EventDispatcher) -> Unit)? = null): Set<suspend Event.(EventDispatcher) -> Unit>? {
        if (depth == event.size()) {
            if (node.handler!!.contains(skip)) {
                return null
            }
            return node.handler
        }
        val param = event.parameter(dispatcher, depth)
        val exact = node.children[param]
        if (exact != null) {
            val result = search(dispatcher, event, exact, depth + 1, skip)
            if (result != null) {
                return result
            }
        }
        for ((key, child) in node.children) {
            if (key == "*" || key == param) {
                continue
            }
            if (wildcardEquals(key, param)) {
                val result = search(dispatcher, event, child, depth + 1, skip)
                if (result != null) {
                    return result
                }
            }
        }
        val default = node.children["*"]
        if (default != null) {
            val result = search(dispatcher, event, default, depth + 1, skip)
            if (result != null) {
                return result
            }
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
        var events = Events()
            private set

        fun setEvents(events: Events) {
            this.events = events
        }

        @Suppress("UNCHECKED_CAST")
        @JvmName("handleDispatcher")
        fun <D : EventDispatcher, E> handle(vararg parameters: String, skipSelf: Boolean = false, block: suspend E.(D) -> Unit) where E : Event, E : CharacterContext {
            val handler = block as suspend Event.(EventDispatcher) -> Unit
            handle(parameters, skipSelf, handler)
        }

        @Suppress("UNCHECKED_CAST")
        @JvmName("handleEvent")
        fun <E> handle(vararg parameters: String, skipSelf: Boolean = false, block: suspend E.(EventDispatcher) -> Unit) where E : Event, E : CharacterContext {
            val handler = block as suspend Event.(EventDispatcher) -> Unit
            handle(parameters, skipSelf, handler)
        }

        private fun handle(parameters: Array<out String>, skipSelf: Boolean = false, handler: suspend Event.(EventDispatcher) -> Unit) {
            if (skipSelf) {
                // Continue onto the next handler after the current by searching handlers again but skipping itself
                var self: (suspend Event.(EventDispatcher) -> Unit)? = null
                self = handler@{ entity ->
                    handler.invoke(this, entity)
                    if (this is CancellableEvent && this.cancelled) {
                        return@handler
                    }
                    val handlers = events.search(entity, this, self!!) ?: return@handler
                    for (h in handlers) {
                        if (entity is CancellableEvent && entity.cancelled) {
                            break
                        }
                        h.invoke(this, entity)
                    }
                }
                events.insert(parameters, self)
            } else {
                events.insert(parameters, handler)
            }
        }
    }
}