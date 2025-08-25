package world.gregs.voidps.engine.event

import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

class Trie(
    val children: Map<Any?, Trie>? = null,
    var handler: Set<suspend Event.(EventDispatcher) -> Unit>? = null
) {
    constructor(vararg nodes: Pair<String, Trie>, handlers: Set<suspend Event.(EventDispatcher) -> Unit>? = null) : this(nodes.toMap(), handlers)

    fun first(dispatcher: EventDispatcher, event: Event, node: Trie, depth: Int, skip: (suspend Event.(EventDispatcher) -> Unit)? = null): Set<suspend Event.(EventDispatcher) -> Unit>? {
        if (depth == event.size) {
            if (node.handler!!.contains(skip)) {
                return null
            }
            return node.handler
        }
        val param = event.parameter(dispatcher, depth)
        val exact = (node.children ?: return null)[param]
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

    fun all(
        dispatcher: EventDispatcher,
        event: Event,
        node: Trie,
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
        for ((key, child) in node.children ?: return output) {
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
}