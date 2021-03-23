package world.gregs.voidps.engine.event

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */
data class EventHandlerBuilder<E : Event>(private var filter: (E.() -> Boolean)? = null) {

    /**
     * Append [EventHandler] with a filter
     */
    infix fun where(filter: E.() -> Boolean) = apply { this.filter = filter }

    fun build(action: E.(E) -> Unit): EventHandler<E> {
        val handler = EventHandler<E>()
        handler.action = action
        handler.filter = filter
        return handler
    }
}

/**
 * Create an [EventHandler] with a filter
 */
inline infix fun <E : Event, reified C : EventCompanion<E>> C.where(noinline filter: E.() -> Boolean) = EventHandlerBuilder(filter = filter)