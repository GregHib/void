package world.gregs.voidps.engine.dispatch

/**
 * A dispatcher collects a number of instances with interface [T] to be stored and called at a later time
 */
@Suppress("UNCHECKED_CAST")
interface Dispatcher<T : Any> {
    fun process(instance: T)
    fun load(instance: Any) = process(instance as T)
    fun clear()
}
