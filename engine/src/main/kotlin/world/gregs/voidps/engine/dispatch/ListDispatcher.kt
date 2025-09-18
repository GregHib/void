package world.gregs.voidps.engine.dispatch

/**
 * A basic implementation of [Dispatcher] which collects [instances] for the parent class to use
 */
abstract class ListDispatcher<T : Any> : Dispatcher<T> {
    protected val instances = mutableListOf<T>()

    override fun process(instance: T) {
        instances.add(instance)
    }
}