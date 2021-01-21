package world.gregs.void.engine.event

/**
 * @author GregHib <greg@gregs.world>
 * @since March 26, 2020
 */
abstract class Event<T : Any> {
    var cancelled = false
        private set

    var result: T? = null

    fun cancel() {
        cancelled = true
    }
}