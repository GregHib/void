package rs.dusk.engine.event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */
class EventHandler<T : Event> {
    var next: EventHandler<T>? = null
    lateinit var action: (T) -> Unit
    var priority: Int = 0
}

