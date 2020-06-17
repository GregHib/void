package rs.dusk.engine.event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
data class EventHandlerBuilder<T : Event>(var filter: (T.() -> Boolean)? = null, var priority: Int = 0)

/**
 * Create an [EventHandler] with priority
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> C.priority(priority: Int) = EventHandlerBuilder<T>(priority = priority)

/**
 * Create an [EventHandler] with a filter
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> C.where(noinline filter: T.() -> Boolean) = EventHandlerBuilder(filter = filter)

/**
 * Append [EventHandler] with a filter
 */
infix fun <T : Event> EventHandlerBuilder<T>.where(filter: T.() -> Boolean) = this.apply { this.filter = filter }

fun <T : Event> on(priority: Int = 0) = EventHandlerBuilder<T>(priority = priority)