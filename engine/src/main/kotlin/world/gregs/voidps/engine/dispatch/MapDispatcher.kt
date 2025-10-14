package world.gregs.voidps.engine.dispatch

/**
 * A common [Dispatcher] which collects [instances] associated with annotation arguments for the parent class to use
 */
class MapDispatcher<T : Any>(val id: String) : Dispatcher<T> {
    val instances = mutableMapOf<String, MutableList<T>>()

    override fun process(instance: T, annotation: String, arguments: String) {
        if (annotation == "" || annotation == id) {
            instances.getOrPut(arguments) { mutableListOf() }.add(instance)
        }
    }

    fun forEach(key: String, block: (T) -> Unit) {
        iterate("*", block)
        iterate(key, block)
    }

    private fun iterate(key: String, block: (T) -> Unit) {
        for (instance in instances[key] ?: return) {
            block.invoke(instance)
        }
    }

    override fun clear() {
        instances.clear()
    }
}