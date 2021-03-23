package world.gregs.voidps.engine.event

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */
data class EventHandlerBuilder<T : Any, E : Event<T>>(private var filter: (E.() -> Boolean)? = null, var priority: Int = 0) {

    /**
     * Append [EventHandler] with a filter
     */
    infix fun where(filter: E.() -> Boolean) = apply { this.filter = filter }


    fun build(action: E.(E) -> Unit): EventHandler<T, E> {
        val handler = EventHandler<T, E>()
        handler.action = action
        handler.filter = filter
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