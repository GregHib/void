package world.gregs.voidps.engine.event

/**
 * @author GregHib <greg@gregs.world>
 * @since March 26, 2020
 */
class EventHandler<T : Any, E : Event<T>> {
    var next: EventHandler<T, E>? = null
    var priority: Int = 0
    var filter: (E.() -> Boolean)? = null
    var check: (E.() -> Boolean)? = null
    lateinit var action: E.(E) -> Unit

    fun checked(event: E) = check?.invoke(event) ?: true

    fun applies(event: E) = filter?.invoke(event) != false

    fun invoke(event: E) {
        action.invoke(event, event)
    }
}

