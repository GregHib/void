package rs.dusk.engine.event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
data class EventHandlerBuilder<T : Any, E : Event<T>>(private var filter: (E.() -> Boolean)? = null, private var check: (E.() -> Boolean)? = null, var priority: Int = 0) {

    /**
     * Append [EventHandler] with a filter
     */
    infix fun where(filter: E.() -> Boolean) = apply { this.filter = filter }

    /**
     * Append [EventHandler] with a pre-check
     */
    infix fun check(check: E.() -> Boolean) = apply { this.check = check }

    fun build(action: E.(E) -> Unit): EventHandler<T, E> {
        val handler = EventHandler<T, E>()
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
inline infix fun <T : Any, E : Event<T>, reified C : EventCompanion<E>> C.priority(priority: Int) = EventHandlerBuilder<T, E>(priority = priority)

/**
 * Create an [EventHandler] with a filter
 */
inline infix fun <T : Any, E : Event<T>, reified C : EventCompanion<E>> C.where(noinline filter: E.() -> Boolean) = EventHandlerBuilder(filter = filter)

/**
 * Create an [EventHandler] with a pre-check
 */
inline infix fun <T : Any, E : Event<T>, reified C : EventCompanion<E>> C.check(noinline check: E.() -> Boolean) = EventHandlerBuilder(check = check)

/**
 * Create an [EventHandler] with nested syntax
 */
fun <T : Any, E : Event<T>> on(block: EventHandlerBuilder<T, E>.() -> Unit) = block.invoke(EventHandlerBuilder())