package world.gregs.voidps.engine.dispatch

/**
 * A basic [Dispatcher] which collects [instances] for the parent class to use
 */
class ListDispatcher<T : Any> : Dispatcher<T> {
    val instances = mutableListOf<T>()

    override fun process(instance: T, annotation: String, arguments: String) {
        instances.add(instance)
    }

    override fun clear() {
        instances.clear()
    }
}