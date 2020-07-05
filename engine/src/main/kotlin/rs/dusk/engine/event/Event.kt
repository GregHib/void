package rs.dusk.engine.event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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