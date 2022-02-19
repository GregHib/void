package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.Entity
import kotlin.reflect.KClass

class Events(
    private val entity: Entity,
) {
    private lateinit var events: Map<KClass<out Event>, List<EventHandler>>
    var all: ((Event) -> Unit)? = null

    operator fun get(klass: KClass<out Event>) = events[klass]

    fun set(events: Map<KClass<out Event>, List<EventHandler>>) {
        this.events = events
    }

    fun <E : Event> emit(event: E): Boolean {
        all?.invoke(event)
        var called = false
        events[event::class]
            ?.forEach {
                if (event is CancellableEvent && event.cancelled) {
                    return true
                }
                if (it.condition(event, entity)) {
                    called = true
                    it.block(event, entity)
                }
            }
        return called
    }
}
