package rs.dusk.engine.event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
data class EventHandlerBuilder<T : Event>(private var filter: (T.() -> Boolean)? = null, private var check: (T.() -> Boolean)? = null, var priority: Int = 0) {

    /**
     * Append [EventHandler] with a filter
     */
    infix fun where(filter: T.() -> Boolean) = apply { this.filter = filter }

    /**
     * Append [EventHandler] with a pre-check
     */
    infix fun check(check: T.() -> Boolean) = apply { this.check = check }

    fun build(action: T.(T) -> Unit): EventHandler<T> {
        val handler = EventHandler<T>()
        handler.action = action
        handler.filter = filter
        handler.check = check
        handler.priority = priority
        return handler
    }
}

/**
 * Create an [EventHandler] with priority
 * Note: Highest priority first
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> C.priority(priority: Int) = EventHandlerBuilder<T>(priority = priority)

/**
 * Create an [EventHandler] with a filter
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> C.where(noinline filter: T.() -> Boolean) = EventHandlerBuilder(filter = filter)

/**
 * Create an [EventHandler] with a pre-check
 */
inline infix fun <T : Event, reified C : EventCompanion<T>> C.check(noinline check: T.() -> Boolean) = EventHandlerBuilder(check = check)

/**
 * Create an [EventHandler] with nested syntax
 */
fun <T : Event> on(block: EventHandlerBuilder<T>.() -> Unit) = block.invoke(EventHandlerBuilder())