package world.gregs.voidps.engine.event

/**
 * @author GregHib <greg@gregs.world>
 * @since March 26, 2020
 */
class EventHandler<E : Event> {
    var next: EventHandler<E>? = null
    var filter: (E.() -> Boolean)? = null
    lateinit var action: E.(E) -> Unit

    fun applies(event: E) = filter?.invoke(event) != false

    fun invoke(event: E) {
        action.invoke(event, event)
    }
}

