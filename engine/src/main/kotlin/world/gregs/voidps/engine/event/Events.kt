package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.Entity
import kotlin.reflect.KClass

class Events(
    private val entity: Entity,
    private val events: MutableMap<KClass<out Event>, MutableList<EventHandler>> = mutableMapOf()
) : MutableMap<KClass<out Event>, MutableList<EventHandler>> by events {

    var all: ((Event) -> Unit)? = null

    fun addAll(clazz: KClass<out Event>, values: List<EventHandler>) {
        events.getOrPut(clazz) { mutableListOf() }.addAll(values)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Entity, reified E : Event> on(
        noinline condition: E.(T) -> Boolean = { true },
        noinline block: E.(T) -> Unit
    ): EventHandler {
        val handler = EventHandler(E::class, condition as Event.(Entity) -> Boolean, block as Event.(Entity) -> Unit)
        getOrPut(E::class) { mutableListOf() }.add(handler)
        return handler
    }

    fun remove(handler: EventHandler) {
        events[handler.event]?.remove(handler)
    }

    fun <E : Event> emit(event: E) {
        all?.invoke(event)
        events[event::class]
            ?.filter { it.condition(event, entity) }
            ?.forEach { it.block(event, entity) }
    }

}
