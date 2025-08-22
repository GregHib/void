package world.gregs.voidps.engine.event

class Trie(
    val children: Map<Any?, Trie>? = null,
    var handler: Set<suspend Event.(EventDispatcher) -> Unit>? = null
) {
    constructor(vararg nodes: Pair<String, Trie>, handlers: Set<suspend Event.(EventDispatcher) -> Unit>? = null) : this(nodes.toMap(), handlers)
}