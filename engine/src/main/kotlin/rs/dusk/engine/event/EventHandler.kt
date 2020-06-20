package rs.dusk.engine.event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */
class EventHandler<T : Event> {
    var next: EventHandler<T>? = null
    var priority: Int = 0
    var filter: (T.() -> Boolean)? = null
    lateinit var action: T.(T) -> Unit

    fun executable(event: T) = !event.cancelled && filter?.invoke(event) != false

    fun invoke(event: T) {
        action.invoke(event, event)
    }
}

