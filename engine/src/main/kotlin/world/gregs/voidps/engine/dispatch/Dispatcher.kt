package world.gregs.voidps.engine.dispatch

/**
 * A dispatcher collects a number of instances with interface [T] to be stored and called at a later time
 */
@Suppress("UNCHECKED_CAST")
interface Dispatcher<T : Any> {
    fun process(instance: T, annotation: String, arguments: String) {
    }

    fun load(instance: Any, annotation: String, arguments: String) {
        process(instance as T, annotation, arguments)
    }

    fun clear()
}
