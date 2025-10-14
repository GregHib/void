package world.gregs.voidps.engine.dispatch

/**
 * A common [Dispatcher] which collects [instances] associated with annotation arguments for the parent class to use
 */
open class MapDispatcher<T : Any>(val ids: Set<String>) : Dispatcher<T> {

    constructor(vararg ids: String) : this(ids.toSet())

    val instances = mutableMapOf<String, MutableList<T>>()

    override fun process(instance: T, annotation: String, arguments: String) {
        if (ids.contains(annotation)) {
            instances.getOrPut(arguments) { mutableListOf() }.add(instance)
        } else if (annotation == "") {
            error("Expected at least one annotation for: $instance, supported annotations: ${ids.toList()}")
        }
    }

    fun forEach(vararg keys: String, block: (T) -> Unit) {
        for (key in keys) {
            iterate(key, block)
        }
    }

    private fun iterate(key: String, block: (T) -> Unit) {
        for (instance in instances[key] ?: return) {
            block.invoke(instance)
        }
    }

    suspend fun onFirst(vararg keys: String, block: suspend (T) -> Unit) {
        for (key in keys) {
            if (first(key, block)) {
                return
            }
        }
    }

    private suspend fun first(key: String, block: suspend (T) -> Unit): Boolean {
        for (instance in instances[key] ?: return false) {
            block.invoke(instance)
            return true
        }
        return false
    }

    override fun clear() {
        instances.clear()
    }
}