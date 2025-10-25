package world.gregs.voidps.engine.dispatch

/**
 * A [Dispatcher] which delegates to multiple sub [dispatchers]
 */
open class CombinedDispatcher<T : Any>(vararg val dispatchers: Dispatcher<T>) : Dispatcher<T> {

    override fun process(instance: T, annotation: String, arguments: String) {
        for (dispatcher in dispatchers) {
            dispatcher.process(instance, annotation, arguments)
        }
    }

    override fun clear() {
    }
}