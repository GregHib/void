package world.gregs.voidps.engine.dispatch

/**
 * A basic implementation of [Dispatcher] which collects [instances] for the parent class to use
 */
open class ListDispatcher<T : Any> : Dispatcher<T> {
    val instances = mutableListOf<T>()

    override fun process(instance: T) {
        instances.add(instance)
    }

    override fun clear() {
        instances.clear()
    }
}